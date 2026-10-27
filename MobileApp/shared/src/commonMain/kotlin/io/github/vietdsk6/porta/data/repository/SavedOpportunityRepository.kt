package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.dao.SavedOpportunityDao
import io.github.vietdsk6.porta.data.source.local.entity.SavedOpportunityEntity
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedOpportunityRepository(
    private val savedOpportunityDao: SavedOpportunityDao,
    private val backgroundExecutor: BackgroundExecutor,
) {
    fun observeSavedIds(
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Flow<Set<String>> = savedOpportunityDao.observeSavedIds(profileId).map { it.toSet() }

    suspend fun setSaved(
        opportunityId: String,
        isSaved: Boolean,
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
        savedAtEpochMillis: Long = nowEpochMillis(),
    ): Result<Unit> = backgroundExecutor.execute {
        require(opportunityId.isNotBlank()) { "Opportunity ID cannot be blank." }
        require(profileId.isNotBlank()) { "Profile ID cannot be blank." }

        if (isSaved) {
            val existing = savedOpportunityDao.get(profileId, opportunityId)
            savedOpportunityDao.upsert(
                existing?.copy(savedAtEpochMillis = savedAtEpochMillis)
                    ?: SavedOpportunityEntity(
                        profileId = profileId,
                        opportunityId = opportunityId,
                        savedAtEpochMillis = savedAtEpochMillis,
                    ),
            )
        } else {
            savedOpportunityDao.delete(profileId, opportunityId)
        }
        Result.success(Unit)
    }

    suspend fun clearForProfile(
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        savedOpportunityDao.deleteForProfile(profileId)
        Result.success(Unit)
    }

    suspend fun clearAll(): Result<Unit> = backgroundExecutor.execute {
        savedOpportunityDao.deleteAll()
        Result.success(Unit)
    }
}
