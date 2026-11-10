package io.github.vietdsk6.porta.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.util.file.FileManager
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
    private lateinit var fileManager: RecordingEvidenceFileManager

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
        fileManager = RecordingEvidenceFileManager()
        repository = ProfileRepository(
            profileFactDao = database.profileFactDao(),
            ruleAssertionDao = database.ruleAssertionDao(),
            backgroundExecutor = BackgroundExecutor(Dispatchers.Default),
            fileManager = fileManager,
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

    @Test
    fun `clear profile removes every locally stored fact`() = runTest {
        repository.saveFacts(
            listOf(
                ProfileFact(
                    id = "default:gpa",
                    key = FactKey.GPA,
                    value = FactValue.NumberValue(3.6, "4.0 scale"),
                ),
                ProfileFact(
                    id = "default:ielts_score",
                    key = FactKey.IELTS_SCORE,
                    value = FactValue.NumberValue(6.5, "band"),
                ),
            ),
        ).getOrThrow()

        assertTrue(repository.clearProfile().isSuccess)

        assertTrue(repository.getProfile().getOrThrow().facts.isEmpty())
    }

    @Test
    fun `profile snapshot combines facts with programme scoped assertions`() = runTest {
        val ruleRef = RuleRef(
            opportunityId = "oecd-internship",
            ruleId = "programme-availability",
        )
        repository.saveFacts(
            listOf(
                ProfileFact(
                    id = "default:gpa",
                    key = FactKey.GPA,
                    value = FactValue.NumberValue(3.6, "4.0 scale"),
                ),
            ),
        ).getOrThrow()
        RuleAssertionRepository(
            ruleAssertionDao = database.ruleAssertionDao(),
            backgroundExecutor = BackgroundExecutor(Dispatchers.Default),
        ).saveAssertions(
            listOf(
                RuleAssertion(
                    ruleRef = ruleRef,
                    ruleFingerprint = "sha256:programme-availability",
                    factKey = FactKey.AVAILABLE_FULL_PERIOD,
                    value = FactValue.BooleanValue(true),
                    evidence = EvidenceMetadata(label = "University approval"),
                    updatedAtEpochMillis = 43L,
                ),
            ),
        ).getOrThrow()

        val saved = repository.getProfile().getOrThrow()

        assertEquals(FactValue.NumberValue(3.6, "4.0 scale"), saved.facts[FactKey.GPA]?.value)
        assertEquals(FactValue.BooleanValue(true), saved.ruleAssertions[ruleRef]?.value)
    }

    @Test
    fun `clear profile removes facts and scoped assertions`() = runTest {
        val ruleRef = RuleRef(
            opportunityId = "cern-summer",
            ruleId = "full-period-availability",
        )
        repository.saveFacts(
            listOf(
                ProfileFact(
                    id = "default:gpa",
                    key = FactKey.GPA,
                    value = FactValue.NumberValue(8.5, "10.0 scale"),
                ),
            ),
        ).getOrThrow()
        RuleAssertionRepository(
            ruleAssertionDao = database.ruleAssertionDao(),
            backgroundExecutor = BackgroundExecutor(Dispatchers.Default),
        ).saveAssertions(
            listOf(
                RuleAssertion(
                    ruleRef = ruleRef,
                    ruleFingerprint = "sha256:full-period-availability",
                    factKey = FactKey.AVAILABLE_FULL_PERIOD,
                    value = FactValue.BooleanValue(true),
                ),
            ),
        ).getOrThrow()

        repository.clearProfile().getOrThrow()

        val cleared = repository.getProfile().getOrThrow()
        assertTrue(cleared.facts.isEmpty())
        assertTrue(cleared.ruleAssertions.isEmpty())
    }

    @Test
    fun `replacing and deleting evidence metadata cleans up app-private files after database writes`() = runTest {
        val oldAttachment = attachment("old.pdf", "default/old.pdf")
        val newAttachment = attachment("new.pdf", "default/new.pdf")
        val original = ProfileFact(
            id = "default:ielts_score",
            key = FactKey.IELTS_SCORE,
            value = FactValue.NumberValue(6.5, "band"),
            evidence = EvidenceMetadata("Score report", attachment = oldAttachment),
        )
        repository.saveFacts(listOf(original)).getOrThrow()

        repository.saveFacts(
            listOf(original.copy(evidence = EvidenceMetadata("New score report", attachment = newAttachment))),
        ).getOrThrow()
        assertEquals(listOf(oldAttachment), fileManager.deletedAttachments)

        repository.deleteFact(FactKey.IELTS_SCORE).getOrThrow()
        assertEquals(listOf(oldAttachment, newAttachment), fileManager.deletedAttachments)
    }

    private fun attachment(name: String, storageKey: String) = EvidenceAttachmentMetadata(
        displayName = name,
        mimeType = "application/pdf",
        byteSize = 10L,
        storageKey = storageKey,
    )
}

private class RecordingEvidenceFileManager : FileManager {
    val deletedAttachments = mutableListOf<EvidenceAttachmentMetadata>()

    override suspend fun saveTextByPickingUpLocation(
        suggestedName: String,
        extension: String,
        content: String,
    ): Result<Boolean> = Result.success(false)

    override suspend fun deleteEvidenceAttachment(
        attachment: EvidenceAttachmentMetadata,
    ): Result<Unit> {
        deletedAttachments += attachment
        return Result.success(Unit)
    }
}
