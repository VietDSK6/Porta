package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.dao.RuleAssertionDao
import io.github.vietdsk6.porta.data.source.local.entity.toEntity
import io.github.vietdsk6.porta.data.source.local.entity.toModel
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.util.file.FileManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RuleAssertionRepository(
    private val ruleAssertionDao: RuleAssertionDao,
    private val backgroundExecutor: BackgroundExecutor,
    private val fileManager: FileManager? = null,
) {
    fun observeAssertions(
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Flow<Map<RuleRef, RuleAssertion>> = ruleAssertionDao.observeForProfile(profileId).map { entities ->
        entities.map { it.toModel() }.associateBy(RuleAssertion::ruleRef)
    }

    suspend fun getAssertions(
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Map<RuleRef, RuleAssertion>> = backgroundExecutor.execute {
        val assertions = ruleAssertionDao.getForProfile(profileId).map { it.toModel() }
        Result.success(assertions.associateBy(RuleAssertion::ruleRef))
    }

    suspend fun saveAssertions(assertions: List<RuleAssertion>): Result<Unit> = backgroundExecutor.execute {
        require(assertions.map(RuleAssertion::profileId).distinct().size <= 1) {
            "All assertions in one save must belong to the same profile."
        }
        val replacedAttachments = assertions.mapNotNull { assertion ->
            ruleAssertionDao.get(
                profileId = assertion.profileId,
                opportunityId = assertion.ruleRef.opportunityId,
                ruleId = assertion.ruleRef.ruleId,
            )?.toModel()
                ?.evidence
                ?.attachment
                ?.takeIf { old -> old.storageKey != assertion.evidence?.attachment?.storageKey }
        }
        ruleAssertionDao.upsertAll(assertions.map(RuleAssertion::toEntity))
        replacedAttachments.forEach { attachment ->
            fileManager?.deleteEvidenceAttachment(attachment)
        }
        Result.success(Unit)
    }

    suspend fun deleteAssertion(
        ruleRef: RuleRef,
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        val attachment = ruleAssertionDao.get(
            profileId = profileId,
            opportunityId = ruleRef.opportunityId,
            ruleId = ruleRef.ruleId,
        )?.toModel()?.evidence?.attachment
        ruleAssertionDao.delete(
            profileId = profileId,
            opportunityId = ruleRef.opportunityId,
            ruleId = ruleRef.ruleId,
        )
        attachment?.let { fileManager?.deleteEvidenceAttachment(it) }
        Result.success(Unit)
    }

    suspend fun clearForProfile(
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        val attachments = ruleAssertionDao.getForProfile(profileId)
            .mapNotNull { entity -> entity.toModel().evidence?.attachment }
        ruleAssertionDao.deleteForProfile(profileId)
        attachments.forEach { attachment ->
            fileManager?.deleteEvidenceAttachment(attachment)
        }
        Result.success(Unit)
    }
}
