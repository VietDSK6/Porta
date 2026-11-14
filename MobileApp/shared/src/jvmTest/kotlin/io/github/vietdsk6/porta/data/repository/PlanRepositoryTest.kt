@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.PlannedRoute
import io.github.vietdsk6.porta.domain.model.RouteExplanation
import io.github.vietdsk6.porta.domain.model.RouteMetrics
import io.github.vietdsk6.porta.domain.model.RouteStep
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlanRepositoryTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: PlanRepository

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        repository = PlanRepository(
            planItemDao = database.planItemDao(),
            backgroundExecutor = BackgroundExecutor(dispatcher),
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `replacing a current plan keeps exactly one local milestone`() = runTest(dispatcher) {
        assertTrue(repository.replaceCurrentPlan(plan(id = "first", title = "First")).isSuccess)
        assertTrue(repository.replaceCurrentPlan(plan(id = "second", title = "Second")).isSuccess)

        assertEquals(listOf("second"), repository.observePlans().first().map(PlanItem::id))
        assertEquals("Second", repository.observeCurrentPlan().first()?.title)
    }

    @Test
    fun `status changes and removal persist`() = runTest(dispatcher) {
        repository.replaceCurrentPlan(plan()).getOrThrow()

        repository.updateStatus("plan", PlanStatus.PAUSED, updatedAtEpochMillis = 200L).getOrThrow()
        assertEquals(PlanStatus.PAUSED, repository.observeCurrentPlan().first()?.status)

        repository.remove("plan").getOrThrow()
        assertNull(repository.observeCurrentPlan().first())
    }

    @Test
    fun `clearing profile plans removes the active milestone`() = runTest(dispatcher) {
        repository.replaceCurrentPlan(plan()).getOrThrow()

        assertTrue(repository.clearForProfile().isSuccess)

        assertNull(repository.observeCurrentPlan().first())
    }

    @Test
    fun `saving a route persists its ordered steps and replaces the prior plan`() = runTest(dispatcher) {
        repository.replaceCurrentPlan(plan()).getOrThrow()
        val first = ActionTemplate(
            id = "raise-ielts",
            title = "Raise IELTS",
            targetFactKey = FactKey.IELTS_SCORE,
            targetValue = FactValue.NumberValue(7.0, "band"),
            targetActionability = Actionability.CONTROLLABLE,
            minimumDurationDays = 30,
            effort = EffortLevel.MEDIUM,
            costBand = CostBand.LOW,
        )
        val second = ActionTemplate(
            id = "complete-degree",
            title = "Complete degree",
            targetFactKey = FactKey.DEGREE_COMPLETED,
            targetValue = FactValue.BooleanValue(true),
            targetActionability = Actionability.CONTROLLABLE,
            minimumDurationDays = 60,
            effort = EffortLevel.HIGH,
            costBand = CostBand.MEDIUM,
        )
        val route = PlannedRoute(
            id = "raise-ielts/complete-degree",
            steps = listOf(
                RouteStep(first, LocalDate(2026, 11, 11), LocalDate(2026, 12, 11), listOf("door-a"), emptyList(), emptyList()),
                RouteStep(second, LocalDate(2026, 12, 11), LocalDate(2027, 2, 9), listOf("door-b"), emptyList(), emptyList()),
            ),
            metrics = RouteMetrics(2, 0, 30, 90, 3, EffortLevel.HIGH),
            explanation = RouteExplanation.OpensDoors(2),
        )

        repository.saveRoute(route).getOrThrow()

        val plans = repository.observePlans().first().sortedBy { it.sequence }
        assertEquals(listOf("raise-ielts", "complete-degree"), plans.map(PlanItem::actionTemplateId))
        assertEquals(listOf(0, 1), plans.map(PlanItem::sequence))
        assertTrue(plans.all { it.routeId == route.id })
        assertEquals(listOf(30, 60), plans.map(PlanItem::expectedDurationDays))

        repository.updateStatus(plans.first().id, PlanStatus.COMPLETED, updatedAtEpochMillis = 200L).getOrThrow()
        assertEquals(plans.first().id, repository.observeCurrentPlan().first()?.id)
        assertEquals(PlanStatus.COMPLETED, repository.observePlan(plans.first().id).first()?.status)
        assertEquals(plans.last().id, repository.observePlan(plans.last().id).first()?.id)
        assertNull(repository.observePlan("missing-step").first())
    }

    private fun plan(
        id: String = "plan",
        title: String = "Raise IELTS to 7.0",
    ): PlanItem = PlanItem(
        id = id,
        actionTemplateId = "raise-ielts-aurora",
        title = title,
        targetFactKey = FactKey.IELTS_SCORE,
        targetValue = FactValue.NumberValue(7.0, "band"),
        expectedDurationDays = 45,
        effort = EffortLevel.MEDIUM,
        costBand = CostBand.LOW,
        createdAtEpochMillis = 100L,
        updatedAtEpochMillis = 100L,
    )
}
