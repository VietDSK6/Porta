package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.dao.OpportunityPackDao
import io.github.vietdsk6.porta.data.source.local.entity.OpportunityPackEntity
import io.github.vietdsk6.porta.data.source.local.pack.ActionTemplateDto
import io.github.vietdsk6.porta.data.source.local.pack.DeadlineDto
import io.github.vietdsk6.porta.data.source.local.pack.FactValueDto
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityDto
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackDto
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.data.source.local.pack.RuleNodeDto
import io.github.vietdsk6.porta.domain.model.OpportunityPackImportStatus
import io.github.vietdsk6.porta.domain.model.OpportunityPackVersionConflictException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpportunityPackRepositoryTest {
    private val codec = OpportunityPackCodec()

    @Test
    fun `invalid pack never writes to storage`() = runTest {
        val dao = FakeOpportunityPackDao()
        val repository = repository(dao)

        val result = repository.importPack("{ not-json", importedAtEpochMillis = 10L)

        assertTrue(result.isFailure)
        assertEquals(0, dao.upsertCount)
        assertNull(dao.getActive())
    }

    @Test
    fun `valid import is atomic and importing identical version is a no-op`() = runTest {
        val dao = FakeOpportunityPackDao()
        val repository = repository(dao)
        val rawJson = codec.encode(validPack())

        val first = repository.importPack(rawJson, importedAtEpochMillis = 10L).getOrThrow()
        val second = repository.importPack(rawJson, importedAtEpochMillis = 20L).getOrThrow()

        assertEquals(OpportunityPackImportStatus.IMPORTED, first.status)
        assertEquals(OpportunityPackImportStatus.NO_CHANGE, second.status)
        assertEquals(1, dao.upsertCount)
        assertEquals(rawJson, dao.getActive()?.rawJson)
    }

    @Test
    fun `preview reports a version diff without activating the candidate`() = runTest {
        val dao = FakeOpportunityPackDao()
        val repository = repository(dao)
        val activeJson = codec.encode(validPack())
        val candidateJson = codec.encode(
            validPack().copy(
                version = 2,
                opportunities = validPack().opportunities.map { opportunity ->
                    opportunity.copy(title = "Updated synthetic door")
                },
            ),
        )
        repository.importPack(activeJson, importedAtEpochMillis = 10L).getOrThrow()

        val preview = repository.previewImport(candidateJson, importedAtEpochMillis = 20L).getOrThrow()

        assertEquals(1, preview.diff.previousVersion)
        assertEquals(2, preview.diff.candidateVersion)
        assertEquals(setOf("synthetic-door"), preview.diff.changedOpportunityIds)
        assertFalse(preview.diff.addedOpportunityIds.isNotEmpty())
        assertEquals(1, dao.upsertCount)
        assertEquals(activeJson, dao.getActive()?.rawJson)

        repository.importPack(candidateJson, importedAtEpochMillis = 20L).getOrThrow()
        assertEquals(2, dao.upsertCount)
        assertEquals(candidateJson, dao.getActive()?.rawJson)
    }

    @Test
    fun `changed content with reused version is rejected`() = runTest {
        val dao = FakeOpportunityPackDao()
        val repository = repository(dao)
        val firstJson = codec.encode(validPack())
        val changedJson = codec.encode(validPack().copy(name = "Changed without version bump"))

        repository.importPack(firstJson, importedAtEpochMillis = 10L).getOrThrow()
        val previewConflict = repository.previewImport(changedJson, importedAtEpochMillis = 20L)
        val conflict = repository.importPack(changedJson, importedAtEpochMillis = 20L)

        assertTrue(previewConflict.isFailure)
        assertIs<OpportunityPackVersionConflictException>(previewConflict.exceptionOrNull())
        assertTrue(conflict.isFailure)
        assertIs<OpportunityPackVersionConflictException>(conflict.exceptionOrNull())
        assertEquals(1, dao.upsertCount)
        assertEquals(firstJson, dao.getActive()?.rawJson)
    }

    @Test
    fun `importing a previously stored inactive pack reactivates it`() = runTest {
        val dao = FakeOpportunityPackDao()
        val repository = repository(dao)
        val bundledJson = codec.encode(validPack())
        val otherJson = codec.encode(validPack().copy(id = "another-pack"))

        repository.importPack(bundledJson, importedAtEpochMillis = 10L).getOrThrow()
        repository.importPack(otherJson, importedAtEpochMillis = 20L).getOrThrow()
        val restored = repository.importPack(bundledJson, importedAtEpochMillis = 30L).getOrThrow()

        assertEquals(OpportunityPackImportStatus.IMPORTED, restored.status)
        assertEquals(3, dao.upsertCount)
        assertEquals(bundledJson, dao.getActive()?.rawJson)
    }

    @Test
    fun `pack with cross-opportunity action collision cannot replace active pack`() = runTest {
        val dao = FakeOpportunityPackDao()
        val repository = repository(dao)
        val validJson = codec.encode(validPack())
        repository.importPack(validJson, importedAtEpochMillis = 10L).getOrThrow()

        val pack = validPack().copy(id = "collision-pack")
        val firstOpportunity = pack.opportunities.single()
        val collisionPack = pack.copy(
            opportunities = listOf(
                firstOpportunity,
                firstOpportunity.copy(
                    id = "second-door",
                    title = "Second synthetic door",
                    sourceUrl = "https://example.invalid/second-door",
                ),
            ),
        )

        val result = repository.importPack(codec.encode(collisionPack), importedAtEpochMillis = 20L)

        assertTrue(result.isFailure)
        assertEquals(1, dao.upsertCount)
        assertEquals(validJson, dao.getActive()?.rawJson)
    }

    private fun repository(dao: OpportunityPackDao): OpportunityPackRepository = OpportunityPackRepository(
        opportunityPackDao = dao,
        parser = OpportunityPackParser(codec, OpportunityPackValidator()),
        backgroundExecutor = BackgroundExecutor(EmptyCoroutineContext),
    )

    private fun validPack(): OpportunityPackDto = OpportunityPackDto(
        schemaVersion = 1,
        id = "porta-repository-test",
        name = "Repository test pack",
        publisher = "Porta tests",
        version = 1,
        lastCheckedOn = "2026-10-20",
        opportunities = listOf(
            OpportunityDto(
                id = "synthetic-door",
                title = "Synthetic door",
                provider = "Test provider",
                category = "SCHOLARSHIP",
                cycle = "test-only",
                sourceUrl = "https://example.invalid/synthetic-door",
                lastCheckedOn = "2026-10-20",
                deadline = DeadlineDto.Known("2027-06-01"),
                eligibilityRule = RuleNodeDto.Atomic(
                    id = "ielts-rule",
                    factKey = "IELTS_SCORE",
                    operator = "GREATER_THAN_OR_EQUAL",
                    expected = FactValueDto.NumberValue(7.0, "band"),
                    explanation = "Synthetic rule.",
                    sourceNote = "Synthetic fixture; not a real requirement.",
                    actionability = "CONTROLLABLE",
                    actionTemplateId = "raise-ielts",
                ),
                actionTemplates = listOf(
                    ActionTemplateDto(
                        id = "raise-ielts",
                        title = "Raise IELTS to 7.0",
                        targetFactKey = "IELTS_SCORE",
                        targetValue = FactValueDto.NumberValue(7.0, "band"),
                        targetActionability = "CONTROLLABLE",
                        minimumDurationDays = 45,
                        effort = "MEDIUM",
                        costBand = "LOW",
                    ),
                ),
            ),
        ),
    )

    private class FakeOpportunityPackDao : OpportunityPackDao {
        private val entities = linkedMapOf<String, OpportunityPackEntity>()
        private val active = MutableStateFlow<OpportunityPackEntity?>(null)
        var upsertCount: Int = 0
            private set

        override fun observeActive(): Flow<OpportunityPackEntity?> = active

        override suspend fun getActive(): OpportunityPackEntity? = active.value

        override suspend fun getByPackIdAndVersion(
            packId: String,
            version: Int,
        ): OpportunityPackEntity? = entities.values.firstOrNull {
            it.packId == packId && it.version == version
        }

        override fun observeAll(): Flow<List<OpportunityPackEntity>> = MutableStateFlow(entities.values.toList())

        override suspend fun upsert(entity: OpportunityPackEntity) {
            upsertCount += 1
            entities[entity.id] = entity
            if (entity.isActive) active.value = entity
        }

        override suspend fun deactivateAll() {
            val inactive = entities.values.map { it.copy(isActive = false) }
            entities.clear()
            inactive.forEach { entities[it.id] = it }
            active.value = null
        }

        override suspend fun deleteById(id: String) {
            val removed = entities.remove(id)
            if (removed?.id == active.value?.id) active.value = null
        }

        override suspend fun deleteAll() {
            entities.clear()
            active.value = null
        }
    }
}
