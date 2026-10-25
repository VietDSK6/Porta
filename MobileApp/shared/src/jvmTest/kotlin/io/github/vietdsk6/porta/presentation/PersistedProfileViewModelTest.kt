@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.data.source.preferences.FakeUserPreferences
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsUiEvent
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsViewModel
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierUiEvent
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierViewModel
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.InformationTaskDestination
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailViewModel
import io.github.vietdsk6.porta.presentation.screens.profilesetup.GpaScaleOption
import io.github.vietdsk6.porta.presentation.screens.profilesetup.GrowthEvidenceEncoding
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupUiEvent
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupViewModel
import io.github.vietdsk6.porta.presentation.screens.profilesetup.StudyLevelOption
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewUiEvent
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewViewModel
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PersistedProfileViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: ProfileRepository
    private lateinit var planRepository: PlanRepository
    private lateinit var savedOpportunityRepository: SavedOpportunityRepository
    private lateinit var opportunityPackRepository: OpportunityPackRepository
    private lateinit var userPreferences: FakeUserPreferences
    private val eligibilityEvaluator = EligibilityEvaluator()
    private val scenarioEvaluator = ScenarioEvaluator(eligibilityEvaluator)
    private val currentLocalDateProvider = CurrentLocalDateProvider { PortaSampleData.referenceDate }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        repository = ProfileRepository(
            profileFactDao = database.profileFactDao(),
            ruleAssertionDao = database.ruleAssertionDao(),
            backgroundExecutor = BackgroundExecutor(dispatcher),
        )
        planRepository = PlanRepository(
            planItemDao = database.planItemDao(),
            backgroundExecutor = BackgroundExecutor(dispatcher),
        )
        savedOpportunityRepository = SavedOpportunityRepository(
            savedOpportunityDao = database.savedOpportunityDao(),
            backgroundExecutor = BackgroundExecutor(dispatcher),
        )
        opportunityPackRepository = OpportunityPackRepository(
            opportunityPackDao = database.opportunityPackDao(),
            parser = OpportunityPackParser(OpportunityPackCodec(), OpportunityPackValidator()),
            backgroundExecutor = BackgroundExecutor(dispatcher),
        )
        userPreferences = FakeUserPreferences()
    }

    @AfterTest
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `doors switch from sample facts to the persisted profile`() = runTest(dispatcher) {
        saveIelts(7.0)

        val viewModel = DoorsViewModel(
            eligibilityEvaluator,
            repository,
            opportunityPackRepository,
            savedOpportunityRepository,
            currentLocalDateProvider,
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSampleMode)
        assertEquals(5, viewModel.uiState.value.openCount)
        assertTrue(viewModel.uiState.value.highlightedDoorIds.isEmpty())
    }

    @Test
    fun `doors react when the persisted profile changes`() = runTest(dispatcher) {
        saveIelts(6.5)
        val viewModel = DoorsViewModel(
            eligibilityEvaluator,
            repository,
            opportunityPackRepository,
            savedOpportunityRepository,
            currentLocalDateProvider,
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.openCount)

        saveIelts(7.0)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSampleMode)
        assertEquals(5, viewModel.uiState.value.openCount)
        assertEquals(4, viewModel.uiState.value.highlightedDoorIds.size)

        viewModel.onUiEvent(DoorsUiEvent.DismissHighlights)

        assertTrue(viewModel.uiState.value.highlightedDoorIds.isEmpty())
    }

    @Test
    fun `doors return to sample mode when the profile is cleared`() = runTest(dispatcher) {
        saveIelts(7.0)
        val viewModel = DoorsViewModel(
            eligibilityEvaluator,
            repository,
            opportunityPackRepository,
            savedOpportunityRepository,
            currentLocalDateProvider,
        )
        advanceUntilIdle()

        repository.clearProfile().getOrThrow()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSampleMode)
        assertEquals(1, viewModel.uiState.value.openCount)
    }

    @Test
    fun `doors evaluate deadlines against the injected current local date`() = runTest(dispatcher) {
        saveIelts(7.0)
        val afterSampleDeadlines = CurrentLocalDateProvider { LocalDate(2027, 6, 2) }

        val viewModel = DoorsViewModel(
            eligibilityEvaluator,
            repository,
            opportunityPackRepository,
            savedOpportunityRepository,
            afterSampleDeadlines,
        )
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.openCount)
        assertTrue(viewModel.uiState.value.doors.all { it.state == EligibilityState.CLOSED })
    }

    @Test
    fun `doors and frontier use the sourced bundled catalog`() = runTest(dispatcher) {
        opportunityPackRepository.importPack(
            BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson(),
        ).getOrThrow()
        savePlanningProfile()

        val doors = DoorsViewModel(
            eligibilityEvaluator,
            repository,
            opportunityPackRepository,
            savedOpportunityRepository,
            currentLocalDateProvider,
        )
        val frontier = FrontierViewModel(
            actionFrontierEngine = ActionFrontierEngine(eligibilityEvaluator, scenarioEvaluator),
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            savedOpportunityRepository = savedOpportunityRepository,
            userPreferences = userPreferences,
            planRepository = planRepository,
            currentLocalDateProvider = currentLocalDateProvider,
        )
        advanceUntilIdle()

        assertFalse(doors.uiState.value.isSampleCatalog)
        assertEquals(15, doors.uiState.value.catalogDoorCount)
        assertTrue(doors.uiState.value.doors.any { it.title == "Kotlin Student Coding Competition" })
        assertFalse(frontier.uiState.value.isSampleCatalog)
        assertEquals(4, frontier.uiState.value.actions.size)
        assertTrue(frontier.uiState.value.actions.all { it.improvedCount == 1 })
        assertEquals(4, frontier.uiState.value.actions.map { it.title }.distinct().size)
        assertTrue(frontier.uiState.value.actions.all { it.title.startsWith("Confirm full-period availability for ") })
        assertTrue(frontier.uiState.value.actions.all { "Porta assumption" in it.durationLabel })
    }

    @Test
    fun `profile setup preserves original scales and optional evidence`() = runTest(dispatcher) {
        val viewModel = ProfileSetupViewModel(repository)
        advanceUntilIdle()

        viewModel.onUiEvent(ProfileSetupUiEvent.StudyLevelChanged(StudyLevelOption.UNDERGRADUATE))
        viewModel.onUiEvent(ProfileSetupUiEvent.FieldOfStudyChanged("Computer Science"))
        viewModel.onUiEvent(ProfileSetupUiEvent.NationalityChanged("VN"))
        viewModel.onUiEvent(ProfileSetupUiEvent.ResidencyChanged("SG"))
        viewModel.onUiEvent(ProfileSetupUiEvent.GpaScaleChanged(GpaScaleOption.TEN))
        viewModel.onUiEvent(ProfileSetupUiEvent.GpaChanged("8.5"))
        viewModel.onUiEvent(ProfileSetupUiEvent.IeltsChanged("6.5"))
        viewModel.onUiEvent(ProfileSetupUiEvent.AgeChanged("20"))
        viewModel.onUiEvent(ProfileSetupUiEvent.ExperienceMonthsChanged("6"))
        viewModel.onUiEvent(ProfileSetupUiEvent.DegreeCompletedChanged(false))
        viewModel.onUiEvent(ProfileSetupUiEvent.CommunityProjectTitleChanged("Local coding club"))
        viewModel.onUiEvent(ProfileSetupUiEvent.CommunityProjectSummaryChanged("Peer Kotlin workshops"))
        viewModel.onUiEvent(ProfileSetupUiEvent.CommunityProjectEvidenceChanged("portfolio/project-1"))
        viewModel.onUiEvent(ProfileSetupUiEvent.Save)
        advanceUntilIdle()

        val saved = repository.getProfile().getOrThrow()
        assertEquals(FactValue.NumberValue(8.5, "10.0 scale"), saved.facts[FactKey.GPA]?.value)
        assertEquals(FactValue.EnumValue("SG"), saved.facts[FactKey.RESIDENCY]?.value)
        assertEquals(FactValue.NumberValue(20.0, "years"), saved.facts[FactKey.AGE]?.value)
        assertEquals(FactValue.NumberValue(6.0, "months"), saved.facts[FactKey.EXPERIENCE_MONTHS]?.value)
        assertEquals(FactValue.BooleanValue(false), saved.facts[FactKey.DEGREE_COMPLETED]?.value)
        val projectValue = saved.facts[FactKey.COMMUNITY_PROJECT]?.value as FactValue.TextValue
        assertEquals("Local coding club", GrowthEvidenceEncoding.decode(projectValue.value).primary)
        assertEquals("portfolio/project-1", saved.facts[FactKey.COMMUNITY_PROJECT]?.evidence?.reference)
        assertNull(saved.facts[FactKey.AVAILABLE_FULL_PERIOD])

        viewModel.onUiEvent(ProfileSetupUiEvent.AgeChanged(""))
        viewModel.onUiEvent(ProfileSetupUiEvent.ExperienceMonthsChanged(""))
        viewModel.onUiEvent(ProfileSetupUiEvent.DegreeCompletedChanged(null))
        viewModel.onUiEvent(ProfileSetupUiEvent.ResidencyChanged(""))
        viewModel.onUiEvent(ProfileSetupUiEvent.CommunityProjectTitleChanged(""))
        viewModel.onUiEvent(ProfileSetupUiEvent.CommunityProjectSummaryChanged(""))
        viewModel.onUiEvent(ProfileSetupUiEvent.CommunityProjectEvidenceChanged(""))
        viewModel.onUiEvent(ProfileSetupUiEvent.Save)
        advanceUntilIdle()

        val updated = repository.getProfile().getOrThrow()
        assertNull(updated.facts[FactKey.AGE])
        assertNull(updated.facts[FactKey.EXPERIENCE_MONTHS])
        assertNull(updated.facts[FactKey.DEGREE_COMPLETED])
        assertNull(updated.facts[FactKey.RESIDENCY])
        assertNull(updated.facts[FactKey.COMMUNITY_PROJECT])
    }

    @Test
    fun `receipt reads the actual persisted value`() = runTest(dispatcher) {
        saveIelts(7.0)

        val viewModel = OpportunityDetailViewModel(
            opportunityId = "aurora",
            eligibilityEvaluator = eligibilityEvaluator,
            actionFrontierEngine = ActionFrontierEngine(eligibilityEvaluator, scenarioEvaluator),
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            currentLocalDateProvider = currentLocalDateProvider,
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSampleProfile)
        assertEquals("7.0 band", viewModel.uiState.value.rules.single().actualLabel)
    }

    @Test
    fun `missing sourced facts create information tasks without claiming profile improvement`() = runTest(dispatcher) {
        opportunityPackRepository.importPack(
            BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson(),
        ).getOrThrow()
        saveIelts(6.5)

        fun detail(opportunityId: String) = OpportunityDetailViewModel(
            opportunityId = opportunityId,
            eligibilityEvaluator = eligibilityEvaluator,
            actionFrontierEngine = ActionFrontierEngine(eligibilityEvaluator, scenarioEvaluator),
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            currentLocalDateProvider = currentLocalDateProvider,
        )

        val daad = detail("daad-epos-2027-2028")
        val kotlinContest = detail("kotlin-student-coding-competition-2026")
        val oecd = detail("oecd-internship-rolling")
        advanceUntilIdle()

        assertEquals(
            setOf(FactKey.DEGREE_COMPLETED, FactKey.EXPERIENCE_MONTHS),
            daad.uiState.value.informationTasks.mapTo(mutableSetOf()) { it.factKey },
        )
        assertTrue(
            kotlinContest.uiState.value.informationTasks.any { task ->
                task.factKey == FactKey.ENROLLED
            },
        )
        assertTrue(
            (daad.uiState.value.informationTasks + kotlinContest.uiState.value.informationTasks)
                .all { task -> task.destination == InformationTaskDestination.ACADEMIC_PROFILE },
        )
        assertTrue(daad.uiState.value.informationTasks.all { it.sourceNote.isNotBlank() })
        assertEquals(
            InformationTaskDestination.OFFICIAL_SOURCE,
            oecd.uiState.value.informationTasks
                .single { it.factKey == FactKey.AVAILABLE_FULL_PERIOD }
                .destination,
        )
        assertNull(daad.uiState.value.relatedActionId)
        assertNull(kotlinContest.uiState.value.relatedActionId)
        assertTrue(daad.uiState.value.nextStepGuidance.contains("not progress"))
        assertEquals(EligibilityState.NEEDS_INFO, daad.uiState.value.state)
        assertTrue(daad.uiState.value.rules.none { rule -> rule.result == RuleResult.PASS })

        val savedFacts = repository.getProfile().getOrThrow().facts
        assertEquals(setOf(FactKey.IELTS_SCORE), savedFacts.keys)
    }

    @Test
    fun `frontier and scenario use persisted facts without mutating them`() = runTest(dispatcher) {
        saveIelts(6.5)
        val frontier = createFrontierViewModel()
        advanceUntilIdle()

        assertFalse(frontier.uiState.value.isSampleMode)
        val action = frontier.uiState.value.actions.first()
        assertEquals("6.5 band", action.currentLabel)
        assertEquals(action.id, frontier.uiState.value.selectedActionId)
        assertTrue(action.doorComparisons.any { it.previewStateLabel == "Assessed rules passed" })
        val scenario = ScenarioPreviewViewModel(
            actionId = action.id,
            scenarioEvaluator = scenarioEvaluator,
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            planRepository = planRepository,
            currentLocalDateProvider = currentLocalDateProvider,
        )
        advanceUntilIdle()

        assertFalse(scenario.uiState.value.isSampleProfile)
        assertEquals("6.5 band", scenario.uiState.value.beforeLabel)
        assertEquals(4, scenario.uiState.value.openedDoors.size)
        assertEquals(
            FactValue.NumberValue(6.5, "band"),
            repository.getProfile().getOrThrow().facts[FactKey.IELTS_SCORE]?.value,
        )
    }

    @Test
    fun `scenario never claims doors excluded by frontier deadline feasibility`() = runTest(dispatcher) {
        saveIelts(6.5)
        val tooLateForSampleActions = CurrentLocalDateProvider { LocalDate(2027, 5, 1) }
        val frontier = FrontierViewModel(
            actionFrontierEngine = ActionFrontierEngine(eligibilityEvaluator, scenarioEvaluator),
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            savedOpportunityRepository = savedOpportunityRepository,
            userPreferences = userPreferences,
            planRepository = planRepository,
            currentLocalDateProvider = tooLateForSampleActions,
        )
        val scenario = ScenarioPreviewViewModel(
            actionId = "raise-ielts-aurora",
            scenarioEvaluator = scenarioEvaluator,
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            planRepository = planRepository,
            currentLocalDateProvider = tooLateForSampleActions,
        )
        advanceUntilIdle()

        assertTrue(frontier.uiState.value.actions.isEmpty())
        assertTrue(frontier.uiState.value.exclusions.isNotEmpty())
        assertTrue(scenario.uiState.value.openedDoors.isEmpty())
        assertEquals(4, scenario.uiState.value.progressedDoors.size)
        assertTrue(scenario.uiState.value.progressedDoors.all { it.timingWarning != null })
        assertEquals(1, scenario.uiState.value.unchangedDoors.size)
    }

    @Test
    fun `scoped scenario names its programme boundary and affects only that door`() = runTest(dispatcher) {
        saveIelts(6.5)
        opportunityPackRepository.importPack(
            BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson(),
        ).getOrThrow()
        val scenario = ScenarioPreviewViewModel(
            actionId = "oecd-full-period-availability",
            scenarioEvaluator = scenarioEvaluator,
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            planRepository = planRepository,
            currentLocalDateProvider = currentLocalDateProvider,
        )
        advanceUntilIdle()

        assertEquals(
            "Confirm full-period availability for OECD Internship Programme",
            scenario.uiState.value.title,
        )
        assertEquals(
            "Would satisfy this programme-specific check if confirmed.",
            scenario.uiState.value.scopeNotice,
        )
        assertEquals(
            listOf("oecd-internship-rolling"),
            (scenario.uiState.value.openedDoors + scenario.uiState.value.progressedDoors)
                .map { it.id },
        )
        val oecd = (scenario.uiState.value.openedDoors + scenario.uiState.value.progressedDoors).single()
        assertEquals(listOf("oecd-availability"), oecd.changedRules.map { it.id })
        assertTrue(oecd.changedRules.single().resultTransitionLabel.contains("Check passed"))
        assertTrue(oecd.remainingBlockers.any { it.startsWith("Still needs information:") })
        assertTrue(oecd.remainingBlockers.any { it.startsWith("Still needs source review:") })
    }

    @Test
    fun `frontier restores and persists all planning constraints`() = runTest(dispatcher) {
        userPreferences.putInt(UserPreferences.KEY_FRONTIER_HORIZON_DAYS, 180)
        userPreferences.putString(UserPreferences.KEY_FRONTIER_MAX_EFFORT, EffortLevel.HIGH.name)
        userPreferences.putString(UserPreferences.KEY_FRONTIER_MAX_COST, CostBand.MEDIUM.name)
        userPreferences.putBoolean(UserPreferences.KEY_FRONTIER_PRIORITIZE_SAVED, false)

        val restored = createFrontierViewModel()
        advanceUntilIdle()

        assertEquals(180, restored.uiState.value.horizonDays)
        assertEquals(EffortLevel.HIGH, restored.uiState.value.maxEffort)
        assertEquals(CostBand.MEDIUM, restored.uiState.value.maxCost)
        assertFalse(restored.uiState.value.prioritizeSaved)

        restored.onUiEvent(FrontierUiEvent.HorizonChanged(30))
        restored.onUiEvent(FrontierUiEvent.MaxEffortChanged(EffortLevel.LOW))
        restored.onUiEvent(FrontierUiEvent.MaxCostChanged(CostBand.FREE))
        restored.onUiEvent(FrontierUiEvent.PrioritizeSavedChanged(true))
        advanceUntilIdle()

        val recreated = createFrontierViewModel()
        advanceUntilIdle()

        assertEquals(30, recreated.uiState.value.horizonDays)
        assertEquals(EffortLevel.LOW, recreated.uiState.value.maxEffort)
        assertEquals(CostBand.FREE, recreated.uiState.value.maxCost)
        assertTrue(recreated.uiState.value.prioritizeSaved)
    }

    @Test
    fun `frontier falls back safely when stored constraints are invalid`() = runTest(dispatcher) {
        userPreferences.putInt(UserPreferences.KEY_FRONTIER_HORIZON_DAYS, 17)
        userPreferences.putString(UserPreferences.KEY_FRONTIER_MAX_EFFORT, "IMPOSSIBLE")
        userPreferences.putString(UserPreferences.KEY_FRONTIER_MAX_COST, "UNBOUNDED")

        val frontier = createFrontierViewModel()
        advanceUntilIdle()

        assertEquals(90, frontier.uiState.value.horizonDays)
        assertEquals(EffortLevel.VERY_HIGH, frontier.uiState.value.maxEffort)
        assertEquals(CostBand.UNKNOWN, frontier.uiState.value.maxCost)
        assertTrue(frontier.uiState.value.prioritizeSaved)
    }

    @Test
    fun `saving a door updates frontier priority data reactively`() = runTest(dispatcher) {
        saveIelts(6.5)
        val doors = DoorsViewModel(
            eligibilityEvaluator,
            repository,
            opportunityPackRepository,
            savedOpportunityRepository,
            currentLocalDateProvider,
        )
        val frontier = createFrontierViewModel()
        advanceUntilIdle()

        assertEquals(0, frontier.uiState.value.actions.single().savedDoorCount)

        doors.onUiEvent(DoorsUiEvent.SavedChanged("aurora"))
        advanceUntilIdle()

        assertTrue(doors.uiState.value.doors.single { it.id == "aurora" }.isSaved)
        assertEquals(1, frontier.uiState.value.actions.single().savedDoorCount)
    }

    @Test
    fun `real profile scenario saves one local plan`() = runTest(dispatcher) {
        saveIelts(6.5)
        val scenario = ScenarioPreviewViewModel(
            actionId = "raise-ielts-aurora",
            scenarioEvaluator = scenarioEvaluator,
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            planRepository = planRepository,
            currentLocalDateProvider = currentLocalDateProvider,
        )
        advanceUntilIdle()

        scenario.onUiEvent(ScenarioPreviewUiEvent.SavePlan)
        advanceUntilIdle()

        assertTrue(scenario.uiState.value.isSaved)
        assertEquals("raise-ielts-aurora", planRepository.observeCurrentPlan().first()?.actionTemplateId)
    }

    private fun createFrontierViewModel(): FrontierViewModel = FrontierViewModel(
        actionFrontierEngine = ActionFrontierEngine(eligibilityEvaluator, scenarioEvaluator),
        profileRepository = repository,
        opportunityPackRepository = opportunityPackRepository,
        savedOpportunityRepository = savedOpportunityRepository,
        userPreferences = userPreferences,
        planRepository = planRepository,
        currentLocalDateProvider = currentLocalDateProvider,
    )

    private suspend fun saveIelts(value: Double) {
        assertTrue(
            repository.saveFacts(
                listOf(
                    ProfileFact(
                        id = "default:ielts_score",
                        key = FactKey.IELTS_SCORE,
                        value = FactValue.NumberValue(value, "band"),
                        actionability = Actionability.CONTROLLABLE,
                    ),
                ),
            ).isSuccess,
        )
    }

    private suspend fun savePlanningProfile() {
        val values = listOf(
            FactKey.AGE to FactValue.NumberValue(20.0, "years"),
            FactKey.ENROLLED to FactValue.BooleanValue(true),
            FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(false),
            FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(6.0, "months"),
            FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(false),
        )
        assertTrue(
            repository.saveFacts(
                values.map { (key, value) ->
                    ProfileFact(
                        id = "default:${key.name.lowercase()}",
                        key = key,
                        value = value,
                        actionability = when (key) {
                            FactKey.AGE -> Actionability.IMMUTABLE
                            FactKey.AVAILABLE_FULL_PERIOD -> Actionability.CONTROLLABLE
                            else -> Actionability.TIME_DEPENDENT
                        },
                    )
                },
            ).isSuccess,
        )
    }
}
