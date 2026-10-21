@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateUiEvent
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateViewModel
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
    private lateinit var opportunityPackRepository: OpportunityPackRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        val backgroundExecutor = BackgroundExecutor(dispatcher)
        planRepository = PlanRepository(database.planItemDao(), backgroundExecutor)
        profileRepository = ProfileRepository(database.profileFactDao(), backgroundExecutor)
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

        val viewModel = ActualProfileUpdateViewModel(planRepository, profileRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasCompletedPlan)
        assertEquals("6.5 band", viewModel.uiState.value.currentValueLabel)
        assertEquals("7.0 band", viewModel.uiState.value.plannedTargetLabel)
        assertEquals(
            FactValue.NumberValue(6.5, "band"),
            profileRepository.getProfile().getOrThrow().facts[FactKey.IELTS_SCORE]?.value,
        )
    }

    @Test
    fun `confirmed achieved value updates doors immediately without recreating the doors view model`() = runTest(dispatcher) {
        saveIelts(6.5)
        planRepository.replaceCurrentPlan(ieltsPlan()).getOrThrow()
        val doors = DoorsViewModel(EligibilityEvaluator(), profileRepository, opportunityPackRepository)
        val update = ActualProfileUpdateViewModel(planRepository, profileRepository)
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
        val viewModel = ActualProfileUpdateViewModel(planRepository, profileRepository)
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.achievedBoolean)
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
    fun `an unfinished plan cannot update the actual profile`() = runTest(dispatcher) {
        planRepository.replaceCurrentPlan(ieltsPlan().copy(status = PlanStatus.IN_PROGRESS)).getOrThrow()
        val viewModel = ActualProfileUpdateViewModel(planRepository, profileRepository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasCompletedPlan)
        assertFalse(viewModel.uiState.value.canConfirm)
        viewModel.onUiEvent(ActualProfileUpdateUiEvent.Confirm)
        advanceUntilIdle()

        assertTrue(profileRepository.getProfile().getOrThrow().facts.isEmpty())
    }

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
        title = "Reserve the full required programme period",
        targetFactKey = FactKey.AVAILABLE_FULL_PERIOD,
        targetValue = FactValue.BooleanValue(true),
        status = PlanStatus.COMPLETED,
        expectedDurationDays = 1,
        effort = EffortLevel.LOW,
        costBand = CostBand.FREE,
        createdAtEpochMillis = 1_700_000_000_000L,
        updatedAtEpochMillis = 1_700_000_000_000L,
    )
}
