package io.github.vietdsk6.porta.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import io.github.vietdsk6.porta.data.source.local.entity.SavedOpportunityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedOpportunityDao {
    @Query(
        "SELECT * FROM saved_opportunity " +
            "WHERE profile_id = :profileId AND opportunity_id = :opportunityId LIMIT 1",
    )
    suspend fun get(profileId: String, opportunityId: String): SavedOpportunityEntity?

    @Query(
        "SELECT opportunity_id FROM saved_opportunity " +
            "WHERE profile_id = :profileId ORDER BY saved_at DESC",
    )
    fun observeSavedIds(profileId: String): Flow<List<String>>

    @Query("SELECT * FROM saved_opportunity WHERE profile_id = :profileId ORDER BY saved_at DESC")
    fun observeSaved(profileId: String): Flow<List<SavedOpportunityEntity>>

    @Upsert
    suspend fun upsert(entity: SavedOpportunityEntity)

    @Query(
        "DELETE FROM saved_opportunity " +
            "WHERE profile_id = :profileId AND opportunity_id = :opportunityId",
    )
    suspend fun delete(profileId: String, opportunityId: String)

    @Query("DELETE FROM saved_opportunity WHERE profile_id = :profileId")
    suspend fun deleteForProfile(profileId: String)

    @Query("DELETE FROM saved_opportunity")
    suspend fun deleteAll()
}
