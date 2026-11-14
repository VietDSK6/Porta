@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanStatusUi
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanUiEvent
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanViewModel
import io.github.vietdsk6.porta.presentation.screens.activeplan.PlanStepState
import io.github.vietdsk6.porta.presentation.screens.activeplan.PlanTimingStatus
import io.github.vietdsk6.porta.presentation.screens.activeplan.projectPlanImpacts
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
        profileRepository = ProfileRepository(
            database.profileFactDao(),
            database.ruleAssertionDao(),
            backgroundExecutor,
        )
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
            currentLocalDateProvider = CurrentLocalDateProvider { PortaSampleData.referenceDate },
        )
        advanceUntilIdle()

        val initial = assertNotNull(viewModel.uiState.value.plan)
        assertEquals(4, initial.candidateOpportunityIds.size)
        assertEquals(4, initial.notRuledOutOpportunityIds.size)
        assertEquals(4, initial.knownDeadlineFeasibleOpportunityIds.size)
        assertEquals(ActivePlanStatusUi.PLANNED, initial.status)
        assertEquals(
            listOf(PlanStepState.DONE, PlanStepState.NEXT, PlanStepState.LATER),
            initial.steps.map { it.state },
        )
        assertEquals("Time estimate · 45 days (Porta assumption)", initial.durationLabel)
        assertEquals("Effort · Medium", initial.effortLabel)
        assertEquals("Cost · Low", initial.costLabel)

        viewModel.onUiEvent(ActivePlanUiEvent.Start)
        advanceUntilIdle()
        assertEquals(ActivePlanStatusUi.IN_PROGRESS, viewModel.uiState.value.plan?.status)
        assertEquals(PlanStepState.IN_PROGRESS, viewModel.uiState.value.plan?.steps?.get(1)?.state)

        viewModel.onUiEvent(ActivePlanUiEvent.Pause)
        advanceUntilIdle()
        assertEquals(ActivePlanStatusUi.PAUSED, viewModel.uiState.value.plan?.status)
        assertEquals(PlanStepState.PAUSED, viewModel.uiState.value.plan?.steps?.get(1)?.state)

        viewModel.onUiEvent(ActivePlanUiEvent.Resume)
        advanceUntilIdle()
        assertEquals(ActivePlanStatusUi.IN_PROGRESS, viewModel.uiState.value.plan?.status)

        viewModel.onUiEvent(ActivePlanUiEvent.Complete)
        advanceUntilIdle()
        assertEquals(ActivePlanStatusUi.COMPLETED, viewModel.uiState.value.plan?.status)
        assertEquals(
            listOf(PlanStepState.DONE, PlanStepState.DONE, PlanStepState.NEXT),
            viewModel.uiState.value.plan?.steps?.map { it.state },
        )
        assertEquals(
            FactValue.NumberValue(6.5, "band"),
            profileRepository.getProfile().getOrThrow().facts[FactKey.IELTS_SCORE]?.value,
        )
    }

    @Test
    fun `five changed checks keep the missed deadline out of four in-plan opportunities`() {
        val opportunities = PortaSampleData.opportunities.map { opportunity ->
            when (opportunity.id) {
                "spark" -> opportunity.copy(deadline = Deadline.Known(LocalDate(2026, 11, 15)))

                "local" -> opportunity.copy(
                    eligibilityRule = (opportunity.eligibilityRule as RuleNode.Atomic).copy(
                        expected = FactValue.NumberValue(7.0, "band"),
                    ),
                )

                else -> opportunity
            }
        }

        val impacts = projectPlanImpacts(
            plan = plan(),
            profile = PortaSampleData.profile,
            opportunities = opportunities,
            today = PortaSampleData.referenceDate,
            scenarioEvaluator = ScenarioEvaluator(EligibilityEvaluator()),
            minimumDurationDays = 45,
        )

        assertEquals(5, impacts.size)
        assertEquals(4, impacts.count { it.timing.isNotRuledOut })
        assertEquals(
            listOf("spark"),
            impacts.filter { it.timing == PlanTimingStatus.MISSES_KNOWN_DEADLINE }.map { it.opportunityId },
        )
        assertEquals(5, impacts.map { it.opportunityId }.distinct().size)
    }

    @Test
    fun `unknown duration remains unverified instead of fitting a known deadline`() {
        val impacts = projectPlanImpacts(
            plan = plan().copy(expectedDurationDays = null),
            profile = PortaSampleData.profile,
            opportunities = PortaSampleData.opportunities,
            today = PortaSampleData.referenceDate,
            scenarioEvaluator = ScenarioEvaluator(EligibilityEvaluator()),
            minimumDurationDays = null,
        )

        assertEquals(4, impacts.size)
        assertEquals(4, impacts.count { it.timing == PlanTimingStatus.TIMING_UNVERIFIED })
        assertEquals(0, impacts.count { it.timing == PlanTimingStatus.WITHIN_KNOWN_DEADLINE })
    }

    @Test
    fun `later route step compares against earlier target rather than actual baseline`() {
        val earlierStep = plan().copy(id = "route:0", routeId = "route", sequence = 0)
        val laterStep = plan().copy(
            id = "route:1",
            routeId = "route",
            sequence = 1,
            target = ActionTarget.ProfileFactTarget(FactKey.IELTS_SCORE, FactValue.NumberValue(8.0, "band")),
            targetDateEpochDays = LocalDate(2027, 1, 1).toEpochDays().toLong(),
        )
        val evaluator = ScenarioEvaluator(EligibilityEvaluator())

        val independentlyProjected = projectPlanImpacts(
            plan = laterStep,
            profile = PortaSampleData.profile,
            opportunities = PortaSampleData.opportunities,
            today = PortaSampleData.referenceDate,
            scenarioEvaluator = evaluator,
            minimumDurationDays = 45,
        )
        val cumulativeProjection = projectPlanImpacts(
            plan = laterStep,
            profile = PortaSampleData.profile,
            opportunities = PortaSampleData.opportunities,
            today = PortaSampleData.referenceDate,
            scenarioEvaluator = evaluator,
            minimumDurationDays = 45,
            priorRouteSteps = listOf(earlierStep),
        )

        assertEquals(4, independentlyProjected.size)
        assertEquals(emptyList(), cumulativeProjection)
    }

    @Test
    fun `saved route step uses its projected end date to flag a missed deadline`() {
        val opportunity = PortaSampleData.opportunities.first().copy(
            deadline = Deadline.Known(LocalDate(2026, 11, 20)),
        )
        val routeStep = plan().copy(
            routeId = "route",
            sequence = 0,
            targetDateEpochDays = LocalDate(2026, 11, 30).toEpochDays().toLong(),
        )

        val impacts = projectPlanImpacts(
            plan = routeStep,
            profile = PortaSampleData.profile,
            opportunities = listOf(opportunity),
            today = PortaSampleData.referenceDate,
            scenarioEvaluator = ScenarioEvaluator(EligibilityEvaluator()),
            minimumDurationDays = 5,
        )

        assertEquals(1, impacts.size)
        assertEquals(PlanTimingStatus.MISSES_KNOWN_DEADLINE, impacts.single().timing)
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
