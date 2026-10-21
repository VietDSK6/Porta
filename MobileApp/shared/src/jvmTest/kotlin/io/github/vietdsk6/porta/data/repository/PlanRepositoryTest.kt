@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
