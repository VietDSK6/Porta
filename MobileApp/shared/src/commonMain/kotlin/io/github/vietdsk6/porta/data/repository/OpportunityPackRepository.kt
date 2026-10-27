package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.dao.OpportunityPackDao
import io.github.vietdsk6.porta.data.source.local.entity.OpportunityPackEntity
import io.github.vietdsk6.porta.data.source.local.entity.toEntity
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.domain.model.InvalidOpportunityPackException
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.OpportunityPackImportPreview
import io.github.vietdsk6.porta.domain.model.OpportunityPackImportResult
import io.github.vietdsk6.porta.domain.model.OpportunityPackImportStatus
import io.github.vietdsk6.porta.domain.model.OpportunityPackVersionConflictException
import io.github.vietdsk6.porta.domain.model.diffFromEmpty
import io.github.vietdsk6.porta.domain.model.diffTo
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import io.github.vietdsk6.porta.util.logging.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class OpportunityPackRepository(
    private val opportunityPackDao: OpportunityPackDao,
    private val parser: OpportunityPackParser,
    private val backgroundExecutor: BackgroundExecutor,
) {
    fun observeActive(): Flow<Result<OpportunityPackDocument?>> = opportunityPackDao.observeActive()
        .map { entity ->
            if (entity == null) {
                Result.success(null)
            } else {
                parser.parse(entity.rawJson, entity.importedAtEpochMillis).toResult()
            }
        }
        .catch { error ->
            AppLogger.e("Error observing active opportunity pack: $error")
            emit(Result.failure(error))
        }

    suspend fun getActive(): Result<OpportunityPackDocument?> = backgroundExecutor.execute {
        val entity = opportunityPackDao.getActive()
            ?: return@execute Result.success(null)
        parser.parse(entity.rawJson, entity.importedAtEpochMillis).toResult()
    }

    suspend fun importPack(
        rawJson: String,
        importedAtEpochMillis: Long = nowEpochMillis(),
    ): Result<OpportunityPackImportResult> = backgroundExecutor.execute {
        val validation = parser.parse(rawJson, importedAtEpochMillis)
        val document = validation.document
            ?: return@execute Result.failure(InvalidOpportunityPackException(validation.diagnostics))
        val metadata = document.metadata
        val existingResult = findCompatibleExisting(metadata.id, metadata.version, rawJson)
        if (existingResult.isFailure) {
            return@execute Result.failure(requireNotNull(existingResult.exceptionOrNull()))
        }
        val existing = existingResult.getOrNull()
        if (existing != null) {
            if (existing.isActive) {
                return@execute Result.success(
                    OpportunityPackImportResult(
                        status = OpportunityPackImportStatus.NO_CHANGE,
                        document = document,
                    ),
                )
            }
        }

        opportunityPackDao.replaceActive(metadata.toEntity())
        Result.success(
            OpportunityPackImportResult(
                status = OpportunityPackImportStatus.IMPORTED,
                document = document,
            ),
        )
    }

    /** Parses and compares a candidate without changing the active catalog. */
    suspend fun previewImport(
        rawJson: String,
        importedAtEpochMillis: Long = nowEpochMillis(),
    ): Result<OpportunityPackImportPreview> = backgroundExecutor.execute {
        val validation = parser.parse(rawJson, importedAtEpochMillis)
        val candidate = validation.document
            ?: return@execute Result.failure(InvalidOpportunityPackException(validation.diagnostics))
        val existingResult = findCompatibleExisting(
            candidate.metadata.id,
            candidate.metadata.version,
            rawJson,
        )
        if (existingResult.isFailure) {
            return@execute Result.failure(requireNotNull(existingResult.exceptionOrNull()))
        }
        val active = opportunityPackDao.getActive()?.let { entity ->
            parser.parse(entity.rawJson, entity.importedAtEpochMillis).document
        }
        Result.success(
            OpportunityPackImportPreview(
                document = candidate,
                diff = active?.diffTo(candidate) ?: candidate.diffFromEmpty(),
            ),
        )
    }

    suspend fun exportActive(): Result<String> = backgroundExecutor.execute {
        val active = opportunityPackDao.getActive()
            ?: return@execute Result.failure(IllegalStateException("No active opportunity pack."))
        Result.success(active.rawJson)
    }

    suspend fun clearAll(): Result<Unit> = backgroundExecutor.execute {
        opportunityPackDao.deleteAll()
        Result.success(Unit)
    }

    private suspend fun findCompatibleExisting(
        packId: String,
        version: Int,
        rawJson: String,
    ): Result<OpportunityPackEntity?> {
        val existing = opportunityPackDao.getByPackIdAndVersion(packId, version)
        if (existing != null && existing.rawJson != rawJson) {
            return Result.failure(OpportunityPackVersionConflictException(packId, version))
        }
        return Result.success(existing)
    }

    private fun io.github.vietdsk6.porta.domain.model.PackValidationResult.toResult(): Result<OpportunityPackDocument?> = document?.let { Result.success(it) }
        ?: Result.failure(InvalidOpportunityPackException(diagnostics))
}
