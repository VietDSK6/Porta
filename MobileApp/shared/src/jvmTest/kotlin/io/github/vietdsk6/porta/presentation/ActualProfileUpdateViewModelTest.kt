@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.RuleAssertionRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.DurationUnit
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.findAtomicRule
import io.github.vietdsk6.porta.domain.model.fingerprint
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateUiEvent
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateViewModel
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualValueInputKind
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsViewModel
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.file.SelectedEvidenceFile
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ActualProfileUpdateViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var planRepository: PlanRepository
    private lateinit var profileRepository: ProfileRepository
    private lateinit var ruleAssertionRepository: RuleAssertionRepository
    private lateinit var savedOpportunityRepository: SavedOpportunityRepository
    private lateinit var opportunityPackRepository: OpportunityPackRepository
    private lateinit var fileManager: FakeEvidenceFileManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        val backgroundExecutor = BackgroundExecutor(dispatcher)
        fileManager = FakeEvidenceFileManager()
        planRepository = PlanRepository(database.planItemDao(), backgroundExecutor)
        profileRepository = ProfileRepository(
            database.profileFactDao(),
            database.ruleAssertionDao(),
            backgroundExecutor,
            fileManager,
        )
        ruleAssertionRepository = RuleAssertionRepository(database.ruleAssertionDao(), backgroundExecutor, fileManager)
        savedOpportunityRepository = SavedOpportunityRepository(database.savedOpportunityDao(), backgroundExecutor)
        opportunityPackRepository = OpportunityPackRepository(
            database.opportunityPackDao(),
            OpportunityPackParser(OpportunityPackCodec(), OpportunityPackValidator()),
            backgroundExecutor,
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `opening and cancelling the review leaves the actual profile unchanged`() = runTest(dispatcher) {
        saveIelts(6.5)
        planRepository.replaceCurrentPlan(ieltsPlan()).getOrThrow()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasCompletedPlan)
        assertEquals("6.5 band", viewModel.uiState.value.currentValueLabel)
        assertEquals("7.0 band", viewModel.uiState.value.plannedTargetLabel)
        assertEquals("", viewModel.uiState.value.achievedValue)
        assertFalse(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()
        assertEquals(
            FactValue.NumberValue(6.5, "band"),
            profileRepository.getProfile().getOrThrow().facts[FactKey.IELTS_SCORE]?.value,
        )
    }

    @Test
    fun `confirmed achieved value updates doors immediately without recreating the doors view model`() = runTest(dispatcher) {
        saveIelts(6.5)
        planRepository.replaceCurrentPlan(ieltsPlan()).getOrThrow()
        val doors = DoorsViewModel(
            EligibilityEvaluator(),
            profileRepository,
            opportunityPackRepository,
            savedOpportunityRepository,
            CurrentLocalDateProvider { PortaSampleData.referenceDate },
        )
        val update = createViewModel()
        advanceUntilIdle()

        assertEquals(1, doors.uiState.value.openCount)
        update.onUiEvent(ActualProfileUpdateUiEvent.AchievedValueChanged("7.0"))
        update.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()

        assertTrue(update.uiState.value.isSaved)
        assertEquals(
            FactValue.NumberValue(7.0, "band"),
            profileRepository.getProfile().getOrThrow().facts[FactKey.IELTS_SCORE]?.value,
        )
        assertEquals(5, doors.uiState.value.openCount)
    }

    @Test
    fun `confirmation can create a boolean fact with optional evidence`() = runTest(dispatcher) {
        saveGpa(3.6)
        planRepository.replaceCurrentPlan(availabilityPlan()).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.achievedBoolean)
        assertFalse(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedBooleanChanged(true))
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.IncludeEvidenceChanged(true))
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.EvidenceLabelChanged("Programme calendar"))
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.EvidenceReferenceChanged("calendar.pdf"))
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()

        val fact = assertNotNull(
            profileRepository.getProfile().getOrThrow().facts[FactKey.AVAILABLE_FULL_PERIOD],
        )
        assertEquals(FactValue.BooleanValue(true), fact.value)
        assertEquals(Actionability.CONTROLLABLE, fact.actionability)
        assertEquals(FactProvenance.EVIDENCE_BACKED, fact.provenance)
        assertEquals("Programme calendar", fact.evidence?.label)
        assertEquals("calendar.pdf", fact.evidence?.reference)
        assertTrue((fact.evidence?.checkedAtEpochMillis ?: 0L) > 0L)
        assertEquals(
            FactValue.NumberValue(3.6, "4.0 scale"),
            profileRepository.getProfile().getOrThrow().facts[FactKey.GPA]?.value,
        )
    }

    @Test
    fun `selected evidence file is copied locally and linked to confirmed fact`() = runTest(dispatcher) {
        planRepository.replaceCurrentPlan(ieltsPlan()).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedValueChanged("7.0"))
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.IncludeEvidenceChanged(true))
        viewModel.onUiEvent(
            ActualProfileUpdateUiEvent.EvidenceFileSelected(
                SelectedEvidenceFile(
                    displayName = "ielts-result.pdf",
                    mimeType = "application/pdf",
                    bytes = byteArrayOf(1, 2, 3, 4),
                ),
            ),
        )

        assertEquals("ielts-result", viewModel.uiState.value.evidenceLabel)
        assertEquals("ielts-result.pdf", viewModel.uiState.value.evidenceAttachment?.displayName)
        assertFalse(viewModel.uiState.value.evidenceAttachment?.isStored == true)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()

        val attachment = assertNotNull(
            profileRepository.getProfile().getOrThrow().facts[FactKey.IELTS_SCORE]?.evidence?.attachment,
        )
        assertEquals("ielts-result.pdf", attachment.displayName)
        assertEquals("application/pdf", attachment.mimeType)
        assertEquals(4L, attachment.byteSize)
        assertNotNull(attachment.storageKey)
        assertEquals(1, fileManager.storedFiles.size)
    }

    @Test
    fun `unsupported evidence file is rejected before local storage`() = runTest(dispatcher) {
        planRepository.replaceCurrentPlan(ieltsPlan()).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(
            ActualProfileUpdateUiEvent.EvidenceFileSelected(
                SelectedEvidenceFile(
                    displayName = "script.exe",
                    mimeType = "application/octet-stream",
                    bytes = byteArrayOf(1),
                ),
            ),
        )

        assertEquals(null, viewModel.uiState.value.evidenceAttachment)
        assertEquals(
            "Unsupported evidence file type. Choose an image, PDF, text, or Word document.",
            viewModel.uiState.value.evidenceAttachmentError,
        )
        assertTrue(fileManager.storedFiles.isEmpty())
    }

    @Test
    fun `programme No result needs no evidence and saves only its scoped rule assertion`() = runTest(dispatcher) {
        saveGpa(3.6)
        val plan = scopedAvailabilityPlan()
        planRepository.replaceCurrentPlan(plan).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Not confirmed", viewModel.uiState.value.currentValueLabel)
        assertTrue(viewModel.uiState.value.factLabel.startsWith("Programme-specific"))
        assertTrue(viewModel.uiState.value.isEvidenceRequired)
        assertTrue(viewModel.uiState.value.scopeDescription?.startsWith("OECD Internship Programme ·") == true)
        assertFalse(viewModel.uiState.value.scopeDescription?.contains("oecd-availability") == true)
        assertEquals(null, viewModel.uiState.value.achievedBoolean)
        assertFalse(viewModel.uiState.value.showEvidenceLabelError)
        assertFalse(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedBooleanChanged(false))
        assertFalse(viewModel.uiState.value.includeEvidence)
        assertFalse(viewModel.uiState.value.requiresSupportingEvidence)
        assertTrue(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()

        val profile = profileRepository.getProfile().getOrThrow()
        val assertion = assertNotNull(
            profile.ruleAssertions[RuleRef("oecd-internship-rolling", "oecd-availability")],
        )
        assertEquals(FactValue.BooleanValue(false), assertion.value)
        assertEquals((plan.target as ActionTarget.OpportunityRuleTarget).ruleFingerprint, assertion.ruleFingerprint)
        assertEquals(null, assertion.evidence)
        assertFalse(FactKey.AVAILABLE_FULL_PERIOD in profile.facts)
    }

    @Test
    fun `programme Yes result requires an evidence title or file`() = runTest(dispatcher) {
        planRepository.replaceCurrentPlan(scopedAvailabilityPlan()).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedBooleanChanged(true))

        assertTrue(viewModel.uiState.value.includeEvidence)
        assertTrue(viewModel.uiState.value.requiresSupportingEvidence)
        assertFalse(viewModel.uiState.value.canConfirm)
        assertEquals(
            "Add an evidence title or supporting file to continue",
            viewModel.uiState.value.confirmationGuidance,
        )

        viewModel.onUiEvent(ActualProfileUpdateUiEvent.EvidenceLabelChanged("Signed programme calendar"))

        assertTrue(viewModel.uiState.value.canConfirm)
    }

    @Test
    fun `programme Yes result accepts a file and derives its evidence title`() = runTest(dispatcher) {
        val plan = scopedAvailabilityPlan()
        planRepository.replaceCurrentPlan(plan).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedBooleanChanged(true))
        viewModel.onUiEvent(
            ActualProfileUpdateUiEvent.EvidenceFileSelected(
                SelectedEvidenceFile(
                    displayName = "signed-calendar.pdf",
                    mimeType = "application/pdf",
                    bytes = byteArrayOf(1, 2, 3, 4),
                ),
            ),
        )
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.EvidenceLabelChanged(""))

        assertTrue(viewModel.uiState.value.hasSupportingEvidence)
        assertTrue(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()

        val assertion = assertNotNull(
            profileRepository.getProfile().getOrThrow().ruleAssertions[
                RuleRef("oecd-internship-rolling", "oecd-availability"),
            ],
        )
        assertEquals(FactValue.BooleanValue(true), assertion.value)
        assertEquals("signed-calendar", assertion.evidence?.label)
        assertEquals("signed-calendar.pdf", assertion.evidence?.attachment?.displayName)
        assertEquals((plan.target as ActionTarget.OpportunityRuleTarget).ruleFingerprint, assertion.ruleFingerprint)
    }

    @Test
    fun `an unfinished plan cannot update the actual profile`() = runTest(dispatcher) {
        planRepository.replaceCurrentPlan(ieltsPlan().copy(status = PlanStatus.IN_PROGRESS)).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasCompletedPlan)
        assertFalse(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()

        assertTrue(profileRepository.getProfile().getOrThrow().facts.isEmpty())
    }

    @Test
    fun `text result starts blank even when the planned target has text`() = runTest(dispatcher) {
        val target = ActionTarget.ProfileFactTarget(
            factKey = FactKey.COMMUNITY_PROJECT,
            value = FactValue.TextValue("Community mentoring"),
        )
        planRepository.replaceCurrentPlan(
            ieltsPlan().copy(
                id = "default:community-project",
                target = target,
            ),
        ).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.achievedValue)
        assertFalse(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedValueChanged("Led a study group"))
        assertTrue(viewModel.uiState.value.canConfirm)
        assertTrue(profileRepository.getProfile().getOrThrow().facts.isEmpty())
    }

    @Test
    fun `profile confirmation preserves enum enum-list text date and duration types`() = runTest(dispatcher) {
        data class FactCase(
            val id: String,
            val key: FactKey,
            val target: FactValue,
            val input: String,
            val expected: FactValue,
            val inputKind: ActualValueInputKind,
            val unitLabel: String = "",
        )

        val cases = listOf(
            FactCase(
                id = "enum",
                key = FactKey.STUDY_LEVEL,
                target = FactValue.EnumValue("MASTER"),
                input = "DOCTORATE",
                expected = FactValue.EnumValue("DOCTORATE"),
                inputKind = ActualValueInputKind.ENUM,
            ),
            FactCase(
                id = "enum-list",
                key = FactKey.FIELD_OF_STUDY,
                target = FactValue.EnumListValue(setOf("POLICY")),
                input = " Policy, Technology, Policy ",
                expected = FactValue.EnumListValue(setOf("Policy", "Technology")),
                inputKind = ActualValueInputKind.ENUM_LIST,
            ),
            FactCase(
                id = "text",
                key = FactKey.COMMUNITY_PROJECT,
                target = FactValue.TextValue("Community mentoring"),
                input = " Led a study group ",
                expected = FactValue.TextValue("Led a study group"),
                inputKind = ActualValueInputKind.TEXT,
            ),
            FactCase(
                id = "date",
                key = FactKey.DATE_OF_BIRTH,
                target = FactValue.DateValue(LocalDate(2000, 1, 1)),
                input = "2001-09-20",
                expected = FactValue.DateValue(LocalDate(2001, 9, 20)),
                inputKind = ActualValueInputKind.DATE,
            ),
            FactCase(
                id = "duration",
                key = FactKey.EXPERIENCE_MONTHS,
                target = FactValue.DurationValue(12, DurationUnit.MONTHS),
                input = "18",
                expected = FactValue.DurationValue(18, DurationUnit.MONTHS),
                inputKind = ActualValueInputKind.DURATION,
                unitLabel = "months",
            ),
        )

        cases.forEach { case ->
            planRepository.replaceCurrentPlan(factPlan(case.id, case.key, case.target)).getOrThrow()
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(case.inputKind, viewModel.uiState.value.inputKind)
            assertEquals(case.unitLabel, viewModel.uiState.value.unitLabel)
            viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedValueChanged(case.input))
            assertEquals(null, viewModel.uiState.value.achievedValueError)
            assertTrue(viewModel.uiState.value.canConfirm)
            viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
            advanceUntilIdle()

            assertEquals(
                case.expected,
                profileRepository.getProfile().getOrThrow().facts[case.key]?.value,
            )
        }
    }

    @Test
    fun `invalid typed input cannot write any part of the profile`() = runTest(dispatcher) {
        data class InvalidCase(
            val id: String,
            val key: FactKey,
            val target: FactValue,
            val input: String,
        )

        val cases = listOf(
            InvalidCase(
                id = "invalid-number",
                key = FactKey.AGE,
                target = FactValue.NumberValue(18.0, "years"),
                input = "NaN",
            ),
            InvalidCase(
                id = "invalid-enum-list",
                key = FactKey.RESIDENCY,
                target = FactValue.EnumListValue(setOf("EU")),
                input = "EU,,EEA",
            ),
            InvalidCase(
                id = "invalid-date",
                key = FactKey.DATE_OF_BIRTH,
                target = FactValue.DateValue(LocalDate(2000, 1, 1)),
                input = "2026-02-30",
            ),
            InvalidCase(
                id = "invalid-duration",
                key = FactKey.EXPERIENCE_MONTHS,
                target = FactValue.DurationValue(12, DurationUnit.MONTHS),
                input = "1.5",
            ),
        )

        cases.forEach { case ->
            planRepository.replaceCurrentPlan(factPlan(case.id, case.key, case.target)).getOrThrow()
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedValueChanged(case.input))

            assertNotNull(viewModel.uiState.value.achievedValueError)
            assertFalse(viewModel.uiState.value.canConfirm)
            viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
            advanceUntilIdle()
            assertFalse(case.key in profileRepository.getProfile().getOrThrow().facts)
        }
    }

    @Test
    fun `programme-scoped date confirmation retains rule fingerprint and does not write a profile fact`() = runTest(dispatcher) {
        opportunityPackRepository.importPack(
            BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson(),
        ).getOrThrow()
        val opportunity = opportunityPackRepository.getActive().getOrThrow()!!.opportunities
            .first { it.id == "schwarzman-scholars-2028-2029" }
        val rule = opportunity.eligibilityRule.findAtomicRule("schwarzman-age-min")!!
        val target = ActionTarget.OpportunityRuleTarget(
            opportunityId = opportunity.id,
            ruleId = rule.id,
            ruleFingerprint = rule.fingerprint(),
            factKey = rule.factKey,
            value = FactValue.DateValue(LocalDate(2001, 9, 20)),
        )
        planRepository.replaceCurrentPlan(
            factPlan("scoped-date", rule.factKey, target.value).copy(target = target),
        ).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(ActualValueInputKind.DATE, viewModel.uiState.value.inputKind)
        assertTrue(viewModel.uiState.value.isEvidenceRequired)
        assertEquals(null, viewModel.uiState.value.scopeWarning)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedValueChanged("2001-09-20"))
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.EvidenceLabelChanged("Passport"))
        assertTrue(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()

        val profile = profileRepository.getProfile().getOrThrow()
        val assertion = assertNotNull(profile.ruleAssertions[RuleRef(opportunity.id, rule.id)])
        assertEquals(FactValue.DateValue(LocalDate(2001, 9, 20)), assertion.value)
        assertEquals(rule.fingerprint(), assertion.ruleFingerprint)
        assertFalse(rule.factKey in profile.facts)
    }

    @Test
    fun `stale scoped target cannot be confirmed into an obsolete assertion`() = runTest(dispatcher) {
        val plan = scopedAvailabilityPlan()
        val stale = (plan.target as ActionTarget.OpportunityRuleTarget).copy(ruleFingerprint = "outdated")
        planRepository.replaceCurrentPlan(plan.copy(target = stale)).getOrThrow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.scopeWarning)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.AchievedBooleanChanged(true))
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.EvidenceLabelChanged("Programme calendar"))
        assertFalse(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()

        assertTrue(profileRepository.getProfile().getOrThrow().ruleAssertions.isEmpty())
    }

    private fun createViewModel() = ActualProfileUpdateViewModel(
        planRepository,
        profileRepository,
        ruleAssertionRepository,
        opportunityPackRepository,
        fileManager,
    )

    private suspend fun saveIelts(value: Double) {
        profileRepository.saveFacts(
            listOf(
                ProfileFact(
                    id = "default:ielts_score",
                    key = FactKey.IELTS_SCORE,
                    value = FactValue.NumberValue(value, "band"),
                    actionability = Actionability.CONTROLLABLE,
                ),
            ),
        ).getOrThrow()
    }

    private suspend fun saveGpa(value: Double) {
        profileRepository.saveFacts(
            listOf(
                ProfileFact(
                    id = "default:gpa",
                    key = FactKey.GPA,
                    value = FactValue.NumberValue(value, "4.0 scale"),
                    actionability = Actionability.CONTROLLABLE,
                ),
            ),
        ).getOrThrow()
    }

    private fun ieltsPlan(): PlanItem = PlanItem(
        id = "default:raise-ielts",
        actionTemplateId = "raise-ielts-aurora",
        title = "Raise IELTS to 7.0 band",
        targetFactKey = FactKey.IELTS_SCORE,
        targetValue = FactValue.NumberValue(7.0, "band"),
        status = PlanStatus.COMPLETED,
        expectedDurationDays = 45,
        effort = EffortLevel.MEDIUM,
        costBand = CostBand.LOW,
        createdAtEpochMillis = 1_700_000_000_000L,
        updatedAtEpochMillis = 1_700_000_000_000L,
    )

    private fun availabilityPlan(): PlanItem = PlanItem(
        id = "default:availability",
        actionTemplateId = "confirm-full-period",
        title = "Confirm full-period availability for OECD Internship Programme",
        targetFactKey = FactKey.AVAILABLE_FULL_PERIOD,
        targetValue = FactValue.BooleanValue(true),
        status = PlanStatus.COMPLETED,
        expectedDurationDays = 1,
        effort = EffortLevel.LOW,
        costBand = CostBand.FREE,
        createdAtEpochMillis = 1_700_000_000_000L,
        updatedAtEpochMillis = 1_700_000_000_000L,
    )

    private fun factPlan(
        id: String,
        key: FactKey,
        target: FactValue,
    ): PlanItem = PlanItem(
        id = "default:$id",
        actionTemplateId = "test-$id",
        title = "Confirm $id",
        targetFactKey = key,
        targetValue = target,
        status = PlanStatus.COMPLETED,
        expectedDurationDays = 1,
        effort = EffortLevel.LOW,
        costBand = CostBand.FREE,
        createdAtEpochMillis = 1_700_000_000_000L,
        updatedAtEpochMillis = 1_700_000_000_000L,
    )

    private suspend fun scopedAvailabilityPlan(): PlanItem {
        opportunityPackRepository.importPack(BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson()).getOrThrow()
        val opportunity = opportunityPackRepository.getActive().getOrThrow()!!.opportunities
            .first { it.id == "oecd-internship-rolling" }
        val rule = opportunity.eligibilityRule.findAtomicRule("oecd-availability")!!
        return PlanItem(
            id = "default:oecd-availability",
            actionTemplateId = "oecd-full-period-availability",
            title = "Reserve the OECD programme period",
            target = ActionTarget.OpportunityRuleTarget(
                opportunityId = "oecd-internship-rolling",
                ruleId = "oecd-availability",
                ruleFingerprint = rule.fingerprint(),
                factKey = FactKey.AVAILABLE_FULL_PERIOD,
                value = FactValue.BooleanValue(true),
            ),
            status = PlanStatus.COMPLETED,
            expectedDurationDays = 14,
            effort = EffortLevel.LOW,
            costBand = CostBand.FREE,
            createdAtEpochMillis = 1_700_000_000_000L,
            updatedAtEpochMillis = 1_700_000_000_000L,
        )
    }
}

private class FakeEvidenceFileManager : FileManager {
    val storedFiles = mutableListOf<EvidenceAttachmentMetadata>()
    val deletedFiles = mutableListOf<EvidenceAttachmentMetadata>()

    override suspend fun saveTextByPickingUpLocation(
        suggestedName: String,
        extension: String,
        content: String,
    ): Result<Boolean> = Result.success(false)

    override suspend fun storeEvidenceAttachment(
        profileId: String,
        selectedFile: SelectedEvidenceFile,
    ): Result<EvidenceAttachmentMetadata> {
        val stored = EvidenceAttachmentMetadata(
            displayName = selectedFile.displayName,
            mimeType = selectedFile.mimeType,
            byteSize = selectedFile.bytes.size.toLong(),
            storageKey = "$profileId/file-${storedFiles.size + 1}.${selectedFile.displayName.substringAfterLast('.')}",
        )
        storedFiles += stored
        return Result.success(stored)
    }

    override suspend fun deleteEvidenceAttachment(
        attachment: EvidenceAttachmentMetadata,
    ): Result<Unit> {
        deletedFiles += attachment
        return Result.success(Unit)
    }
}
