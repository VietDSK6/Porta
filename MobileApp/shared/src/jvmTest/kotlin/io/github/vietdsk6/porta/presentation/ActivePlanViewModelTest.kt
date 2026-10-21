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
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanStatusUi
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanUiEvent
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanViewModel
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
import kotlin.test.assertNotNull

class ActivePlanViewModelTest {
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
    fun `plan progress never changes the actual profile fact`() = runTest(dispatcher) {
        profileRepository.saveFacts(
            listOf(
                ProfileFact(
                    id = "default:ielts",
                    key = FactKey.IELTS_SCORE,
                    value = FactValue.NumberValue(6.5, "band"),
                    actionability = Actionability.CONTROLLABLE,
                ),
            ),
        ).getOrThrow()
        planRepository.replaceCurrentPlan(plan()).getOrThrow()
        val viewModel = ActivePlanViewModel(
            planRepository = planRepository,
            profileRepository = profileRepository,
            opportunityPackRepository = opportunityPackRepository,
            scenarioEvaluator = ScenarioEvaluator(EligibilityEvaluator()),
        )
        advanceUntilIdle()

        val initial = assertNotNull(viewModel.uiState.value.plan)
        assertEquals(4, initial.affectedDoorTitles.size)
        assertEquals(ActivePlanStatusUi.PLANNED, initial.status)

        viewModel.onUiEvent(ActivePlanUiEvent.Start)
        advanceUntilIdle()
        assertEquals(ActivePlanStatusUi.IN_PROGRESS, viewModel.uiState.value.plan?.status)

        viewModel.onUiEvent(ActivePlanUiEvent.Complete)
        advanceUntilIdle()
        assertEquals(ActivePlanStatusUi.COMPLETED, viewModel.uiState.value.plan?.status)
        assertEquals(
            FactValue.NumberValue(6.5, "band"),
            profileRepository.getProfile().getOrThrow().facts[FactKey.IELTS_SCORE]?.value,
        )
    }

    private fun plan(): PlanItem = PlanItem(
        id = "default:raise-ielts",
        actionTemplateId = "raise-ielts-aurora",
        title = "Raise IELTS to 7.0 band",
        targetFactKey = FactKey.IELTS_SCORE,
        targetValue = FactValue.NumberValue(7.0, "band"),
        expectedDurationDays = 45,
        effort = EffortLevel.MEDIUM,
        costBand = CostBand.LOW,
        createdAtEpochMillis = 1_700_000_000_000L,
        updatedAtEpochMillis = 1_700_000_000_000L,
    )
}
