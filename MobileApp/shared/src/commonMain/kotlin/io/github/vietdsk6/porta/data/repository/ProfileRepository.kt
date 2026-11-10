package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.dao.ProfileFactDao
import io.github.vietdsk6.porta.data.source.local.dao.RuleAssertionDao
import io.github.vietdsk6.porta.data.source.local.entity.toEntity
import io.github.vietdsk6.porta.data.source.local.entity.toModel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.util.file.FileManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ProfileRepository(
    private val profileFactDao: ProfileFactDao,
    private val ruleAssertionDao: RuleAssertionDao,
    private val backgroundExecutor: BackgroundExecutor,
    private val fileManager: FileManager? = null,
) {
    fun observeProfile(profileId: String = ProfileFact.DEFAULT_PROFILE_ID): Flow<ProfileSnapshot> = combine(
        profileFactDao.observeProfile(profileId),
        ruleAssertionDao.observeForProfile(profileId),
    ) { factEntities, assertionEntities ->
        val assertions = assertionEntities.map { it.toModel() }
        ProfileSnapshot(
            id = profileId,
            facts = factEntities.map { it.toModel() }.associateBy(ProfileFact::key),
            ruleAssertions = assertions.associateBy { it.ruleRef },
        )
    }

    suspend fun getProfile(profileId: String = ProfileFact.DEFAULT_PROFILE_ID): Result<ProfileSnapshot> = backgroundExecutor.execute {
        val facts = profileFactDao.getProfile(profileId).map { it.toModel() }
        val assertions = ruleAssertionDao.getForProfile(profileId).map { it.toModel() }
        Result.success(
            ProfileSnapshot(
                id = profileId,
                facts = facts.associateBy { it.key },
                ruleAssertions = assertions.associateBy { it.ruleRef },
            ),
        )
    }

    suspend fun saveFacts(facts: List<ProfileFact>): Result<Unit> = backgroundExecutor.execute {
        require(facts.map(ProfileFact::profileId).distinct().size <= 1) {
            "All facts in one save must belong to the same profile."
        }
        val replacedAttachments = facts.mapNotNull { fact ->
            profileFactDao.get(fact.profileId, fact.key.name)
                ?.toModel()
                ?.evidence
                ?.attachment
                ?.takeIf { old -> old.storageKey != fact.evidence?.attachment?.storageKey }
        }
        profileFactDao.upsertAll(facts.map(ProfileFact::toEntity))
        replacedAttachments.forEach { attachment ->
            fileManager?.deleteEvidenceAttachment(attachment)
        }
        Result.success(Unit)
    }

    suspend fun deleteFact(
        key: FactKey,
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        val attachment = profileFactDao.get(profileId, key.name)
            ?.toModel()
            ?.evidence
            ?.attachment
        profileFactDao.delete(profileId, key.name)
        attachment?.let { fileManager?.deleteEvidenceAttachment(it) }
        Result.success(Unit)
    }

    suspend fun clearProfile(
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        profileFactDao.deleteProfile(profileId)
        ruleAssertionDao.deleteForProfile(profileId)
        fileManager?.clearEvidenceAttachments(profileId)
        Result.success(Unit)
    }

    suspend fun clearAll(): Result<Unit> = backgroundExecutor.execute {
        profileFactDao.deleteAll()
        ruleAssertionDao.deleteAll()
        fileManager?.clearEvidenceAttachments()
        Result.success(Unit)
    }
}
