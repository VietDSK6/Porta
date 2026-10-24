package io.github.vietdsk6.porta.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuleAssertionRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: RuleAssertionRepository

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
        repository = RuleAssertionRepository(
            ruleAssertionDao = database.ruleAssertionDao(),
            backgroundExecutor = BackgroundExecutor(Dispatchers.Default),
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `upsert replaces the assertion for the same programme rule`() = runTest {
        val ruleRef = RuleRef(
            opportunityId = "esa-graduate-trainee",
            ruleId = "programme-availability",
        )
        val original = assertion(ruleRef, false)
        val confirmed = original.copy(
            value = FactValue.BooleanValue(true),
            ruleFingerprint = "sha256:v2",
            updatedAtEpochMillis = 2L,
        )

        repository.saveAssertions(listOf(original)).getOrThrow()
        repository.saveAssertions(listOf(confirmed)).getOrThrow()

        val saved = repository.getAssertions().getOrThrow()
        assertEquals(mapOf(ruleRef to confirmed), saved)
    }

    @Test
    fun `assertions with the same rule id stay isolated by opportunity`() = runTest {
        val firstRef = RuleRef("oecd-internship", "programme-availability")
        val secondRef = RuleRef("cern-summer", "programme-availability")

        repository.saveAssertions(
            listOf(
                assertion(firstRef, true),
                assertion(secondRef, false),
            ),
        ).getOrThrow()

        val observed = repository.observeAssertions().first()
        assertEquals(FactValue.BooleanValue(true), observed[firstRef]?.value)
        assertEquals(FactValue.BooleanValue(false), observed[secondRef]?.value)
    }

    @Test
    fun `delete assertion leaves other programme assertions intact`() = runTest {
        val deletedRef = RuleRef("oecd-internship", "programme-availability")
        val retainedRef = RuleRef("cern-summer", "programme-availability")
        repository.saveAssertions(
            listOf(
                assertion(deletedRef, true),
                assertion(retainedRef, true),
            ),
        ).getOrThrow()

        repository.deleteAssertion(deletedRef).getOrThrow()

        val saved = repository.getAssertions().getOrThrow()
        assertTrue(deletedRef !in saved)
        assertTrue(retainedRef in saved)
    }

    private fun assertion(
        ruleRef: RuleRef,
        value: Boolean,
    ): RuleAssertion = RuleAssertion(
        ruleRef = ruleRef,
        ruleFingerprint = "sha256:v1",
        factKey = FactKey.AVAILABLE_FULL_PERIOD,
        value = FactValue.BooleanValue(value),
        updatedAtEpochMillis = 1L,
    )
}
