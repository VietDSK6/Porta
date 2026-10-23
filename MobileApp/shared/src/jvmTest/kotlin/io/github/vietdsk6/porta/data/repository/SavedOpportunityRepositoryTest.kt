@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SavedOpportunityRepositoryTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: SavedOpportunityRepository

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        repository = SavedOpportunityRepository(
            savedOpportunityDao = database.savedOpportunityDao(),
            backgroundExecutor = BackgroundExecutor(dispatcher),
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `saving and unsaving emits the persisted IDs`() = runTest(dispatcher) {
        assertTrue(repository.setSaved("door-one", isSaved = true, savedAtEpochMillis = 100L).isSuccess)
        assertEquals(setOf("door-one"), repository.observeSavedIds().first())

        assertTrue(repository.setSaved("door-one", isSaved = false).isSuccess)
        assertEquals(emptySet(), repository.observeSavedIds().first())
    }

    @Test
    fun `saving an existing door updates it without creating a duplicate`() = runTest(dispatcher) {
        repository.setSaved("door-one", isSaved = true, savedAtEpochMillis = 100L).getOrThrow()
        repository.setSaved("door-one", isSaved = true, savedAtEpochMillis = 200L).getOrThrow()

        assertEquals(setOf("door-one"), repository.observeSavedIds().first())
        assertEquals(1, database.savedOpportunityDao().observeSaved("default").first().size)
        assertEquals(200L, database.savedOpportunityDao().get("default", "door-one")?.savedAtEpochMillis)
    }

    @Test
    fun `saved doors stay isolated by profile`() = runTest(dispatcher) {
        repository.setSaved("door-one", isSaved = true, profileId = "student-a").getOrThrow()
        repository.setSaved("door-two", isSaved = true, profileId = "student-b").getOrThrow()

        assertEquals(setOf("door-one"), repository.observeSavedIds("student-a").first())
        assertEquals(setOf("door-two"), repository.observeSavedIds("student-b").first())
    }
}
