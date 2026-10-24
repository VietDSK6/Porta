package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.dao.RuleAssertionDao
import io.github.vietdsk6.porta.data.source.local.entity.toEntity
import io.github.vietdsk6.porta.data.source.local.entity.toModel
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RuleAssertionRepository(
    private val ruleAssertionDao: RuleAssertionDao,
    private val backgroundExecutor: BackgroundExecutor,
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
        ruleAssertionDao.upsertAll(assertions.map(RuleAssertion::toEntity))
        Result.success(Unit)
    }

    suspend fun deleteAssertion(
        ruleRef: RuleRef,
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        ruleAssertionDao.delete(
            profileId = profileId,
            opportunityId = ruleRef.opportunityId,
            ruleId = ruleRef.ruleId,
        )
        Result.success(Unit)
    }

    suspend fun clearForProfile(
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        ruleAssertionDao.deleteForProfile(profileId)
        Result.success(Unit)
    }
}
