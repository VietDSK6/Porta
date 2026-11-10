@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.data.repository

import androidx.room3.Room
import androidx.room3.executeSQL
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.entity.toEntity
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.DurationUnit
import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileImportRepositoryTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: ProfileImportRepository
    private lateinit var profileRepository: ProfileRepository

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        val backgroundExecutor = BackgroundExecutor(dispatcher)
        repository = ProfileImportRepository(database.profileImportDao(), backgroundExecutor)
        profileRepository = ProfileRepository(
            database.profileFactDao(),
            database.ruleAssertionDao(),
            backgroundExecutor,
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `replace removes stale rows rebases profile id and preserves structured values`() = runTest(dispatcher) {
        val staleFact = fact(
            profileId = ProfileFact.DEFAULT_PROFILE_ID,
            key = FactKey.GPA,
            value = FactValue.NumberValue(2.0, "4.0 scale"),
        )
        val staleAssertion = assertion(
            profileId = ProfileFact.DEFAULT_PROFILE_ID,
            ruleRef = RuleRef("stale-door", "stale-rule"),
            value = FactValue.BooleanValue(false),
        )
        database.profileFactDao().upsert(staleFact.toEntity())
        database.ruleAssertionDao().upsert(staleAssertion.toEntity())

        val sourceProfileId = "android-profile"
        val importedFacts = listOf(
            fact(sourceProfileId, FactKey.ENROLLED, FactValue.BooleanValue(true)),
            fact(sourceProfileId, FactKey.IELTS_SCORE, FactValue.NumberValue(7.0, "band")),
            fact(sourceProfileId, FactKey.STUDY_LEVEL, FactValue.EnumValue("undergraduate")),
            fact(sourceProfileId, FactKey.FIELD_OF_STUDY, FactValue.EnumListValue(setOf("CS", "Math"))),
            fact(sourceProfileId, FactKey.DATE_OF_BIRTH, FactValue.DateValue(LocalDate(2004, 2, 29))),
            fact(sourceProfileId, FactKey.EXPERIENCE_MONTHS, FactValue.DurationValue(18, DurationUnit.MONTHS)),
            fact(
                sourceProfileId,
                FactKey.COMMUNITY_PROJECT,
                FactValue.TextValue("Local coding club"),
                evidence = EvidenceMetadata(
                    label = "Project log",
                    reference = "private/reference",
                    checkedAtEpochMillis = 77L,
                    attachment = EvidenceAttachmentMetadata(
                        displayName = "project-log.pdf",
                        mimeType = "application/pdf",
                        byteSize = 512L,
                        storageKey = "android-profile/private.pdf",
                    ),
                ),
            ),
        )
        val importedAssertion = assertion(
            profileId = sourceProfileId,
            ruleRef = RuleRef("door-1", "rule-1"),
            value = FactValue.DateValue(LocalDate(2026, 12, 1)),
        )
        val result = repository.replaceActiveProfile(
            ProfileSnapshot(
                id = sourceProfileId,
                facts = importedFacts.associateBy(ProfileFact::key),
                ruleAssertions = mapOf(importedAssertion.ruleRef to importedAssertion),
            ),
        )

        assertTrue(result.isSuccess)
        val stored = profileRepository.getProfile().getOrThrow()
        assertEquals(importedFacts.map(ProfileFact::key).toSet(), stored.facts.keys)
        assertEquals(setOf(importedAssertion.ruleRef), stored.ruleAssertions.keys)
        assertTrue(stored.facts.values.all { it.profileId == ProfileFact.DEFAULT_PROFILE_ID })
        assertTrue(stored.ruleAssertions.values.all { it.profileId == ProfileFact.DEFAULT_PROFILE_ID })
        assertEquals(importedFacts.map(ProfileFact::value).toSet(), stored.facts.values.map(ProfileFact::value).toSet())
        assertEquals("Project log", stored.facts.getValue(FactKey.COMMUNITY_PROJECT).evidence?.label)
        val importedAttachment = stored.facts.getValue(FactKey.COMMUNITY_PROJECT).evidence?.attachment
        assertEquals("project-log.pdf", importedAttachment?.displayName)
        assertEquals(512L, importedAttachment?.byteSize)
        assertEquals(null, importedAttachment?.storageKey)
        assertEquals("rule-fingerprint", stored.ruleAssertions.getValue(importedAssertion.ruleRef).ruleFingerprint)
    }

    @Test
    fun `failed assertion insert rolls back deleted facts and assertions`() = runTest(dispatcher) {
        val originalFact = fact(
            profileId = ProfileFact.DEFAULT_PROFILE_ID,
            key = FactKey.GPA,
            value = FactValue.NumberValue(3.5, "4.0 scale"),
        )
        val originalAssertion = assertion(
            profileId = ProfileFact.DEFAULT_PROFILE_ID,
            ruleRef = RuleRef("original-door", "original-rule"),
            value = FactValue.BooleanValue(true),
        )
        database.profileFactDao().upsert(originalFact.toEntity())
        database.ruleAssertionDao().upsert(originalAssertion.toEntity())
        database.useWriterConnection { connection ->
            connection.executeSQL(
                "CREATE TRIGGER reject_imported_assertion " +
                    "BEFORE INSERT ON rule_assertion " +
                    "WHEN NEW.opportunity_id = 'reject-door' " +
                    "BEGIN SELECT RAISE(ABORT, 'forced import failure'); END",
            )
        }

        val rejectedAssertion = assertion(
            profileId = "source",
            ruleRef = RuleRef("reject-door", "rule"),
            value = FactValue.BooleanValue(true),
        )
        val importedFact = fact(
            profileId = ProfileFact.DEFAULT_PROFILE_ID,
            key = FactKey.IELTS_SCORE,
            value = FactValue.NumberValue(8.0, "band"),
        )
        val result = runCatching {
            database.profileImportDao().replaceProfile(
                profileId = ProfileFact.DEFAULT_PROFILE_ID,
                facts = listOf(importedFact.toEntity()),
                assertions = listOf(
                    rejectedAssertion.copy(profileId = ProfileFact.DEFAULT_PROFILE_ID).toEntity(),
                ),
            )
        }

        assertTrue(result.isFailure)
        val stored = profileRepository.getProfile().getOrThrow()
        assertEquals(setOf(FactKey.GPA), stored.facts.keys)
        assertEquals(setOf(originalAssertion.ruleRef), stored.ruleAssertions.keys)
        assertEquals(originalFact.value, stored.facts.getValue(FactKey.GPA).value)
    }

    private fun fact(
        profileId: String,
        key: FactKey,
        value: FactValue,
        evidence: EvidenceMetadata? = null,
    ): ProfileFact = ProfileFact(
        id = "$profileId:${key.name.lowercase()}",
        profileId = profileId,
        key = key,
        value = value,
        provenance = if (evidence == null) FactProvenance.SELF_REPORTED else FactProvenance.EVIDENCE_BACKED,
        actionability = Actionability.CONTROLLABLE,
        evidence = evidence,
        updatedAtEpochMillis = 123L,
    )

    private fun assertion(
        profileId: String,
        ruleRef: RuleRef,
        value: FactValue,
    ): RuleAssertion = RuleAssertion(
        profileId = profileId,
        ruleRef = ruleRef,
        ruleFingerprint = "rule-fingerprint",
        factKey = FactKey.ENROLLED,
        value = value,
        evidence = EvidenceMetadata("Evidence label", "reference", 99L),
        updatedAtEpochMillis = 456L,
    )
}
