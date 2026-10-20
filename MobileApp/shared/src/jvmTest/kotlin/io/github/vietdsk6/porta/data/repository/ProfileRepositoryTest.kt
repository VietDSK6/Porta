package io.github.vietdsk6.porta.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ProfileRepository

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
        repository = ProfileRepository(
            profileFactDao = database.profileFactDao(),
            backgroundExecutor = BackgroundExecutor(Dispatchers.Default),
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `minimal profile facts round trip without becoming sample data`() = runTest {
        val facts = listOf(
            ProfileFact(
                id = "default:gpa",
                key = FactKey.GPA,
                value = FactValue.NumberValue(3.6, "4.0 scale"),
                actionability = Actionability.CONTROLLABLE,
                updatedAtEpochMillis = 42L,
            ),
            ProfileFact(
                id = "default:nationality",
                key = FactKey.NATIONALITY,
                value = FactValue.EnumValue("VN"),
                actionability = Actionability.CONTEXT_ONLY,
                updatedAtEpochMillis = 42L,
            ),
        )

        assertTrue(repository.saveFacts(facts).isSuccess)
        val saved = repository.getProfile().getOrThrow()

        assertEquals(ProfileFact.DEFAULT_PROFILE_ID, saved.id)
        assertEquals(facts.associateBy(ProfileFact::key), saved.facts)
    }

    @Test
    fun `saving a fact with the same key updates the real profile atomically`() = runTest {
        val original = ProfileFact(
            id = "default:ielts_score",
            key = FactKey.IELTS_SCORE,
            value = FactValue.NumberValue(6.5, "band"),
        )
        repository.saveFacts(listOf(original)).getOrThrow()

        repository.saveFacts(
            listOf(original.copy(value = FactValue.NumberValue(7.0, "band"))),
        ).getOrThrow()

        val saved = repository.getProfile().getOrThrow()
        assertEquals(FactValue.NumberValue(7.0, "band"), saved.facts[FactKey.IELTS_SCORE]?.value)
        assertEquals(1, saved.facts.size)
    }
}
