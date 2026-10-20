package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.dao.ProfileFactDao
import io.github.vietdsk6.porta.data.source.local.entity.toEntity
import io.github.vietdsk6.porta.data.source.local.entity.toModel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(
    private val profileFactDao: ProfileFactDao,
    private val backgroundExecutor: BackgroundExecutor,
) {
    fun observeProfile(profileId: String = ProfileFact.DEFAULT_PROFILE_ID): Flow<ProfileSnapshot> = profileFactDao.observeProfile(profileId).map { entities ->
        ProfileSnapshot(
            id = profileId,
            facts = entities.map { it.toModel() }.associateBy(ProfileFact::key),
        )
    }

    suspend fun getProfile(profileId: String = ProfileFact.DEFAULT_PROFILE_ID): Result<ProfileSnapshot> = backgroundExecutor.execute {
        val facts = profileFactDao.getProfile(profileId).map { it.toModel() }
        Result.success(ProfileSnapshot(id = profileId, facts = facts.associateBy { it.key }))
    }

    suspend fun saveFacts(facts: List<ProfileFact>): Result<Unit> = backgroundExecutor.execute {
        require(facts.map(ProfileFact::profileId).distinct().size <= 1) {
            "All facts in one save must belong to the same profile."
        }
        profileFactDao.upsertAll(facts.map(ProfileFact::toEntity))
        Result.success(Unit)
    }

    suspend fun deleteFact(
        key: FactKey,
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        profileFactDao.delete(profileId, key.name)
        Result.success(Unit)
    }
}
