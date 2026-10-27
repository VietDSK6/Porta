package io.github.vietdsk6.porta.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import io.github.vietdsk6.porta.data.source.local.entity.ProfileFactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileFactDao {
    @Query("SELECT * FROM profile_fact WHERE profile_id = :profileId AND fact_key = :factKey")
    suspend fun get(profileId: String, factKey: String): ProfileFactEntity?

    @Query("SELECT * FROM profile_fact WHERE profile_id = :profileId ORDER BY fact_key")
    fun observeProfile(profileId: String): Flow<List<ProfileFactEntity>>

    @Query("SELECT * FROM profile_fact WHERE profile_id = :profileId ORDER BY fact_key")
    suspend fun getProfile(profileId: String): List<ProfileFactEntity>

    @Upsert
    suspend fun upsertAll(entities: List<ProfileFactEntity>)

    @Upsert
    suspend fun upsert(entity: ProfileFactEntity)

    @Query("DELETE FROM profile_fact WHERE profile_id = :profileId AND fact_key = :factKey")
    suspend fun delete(profileId: String, factKey: String)

    @Query("DELETE FROM profile_fact WHERE profile_id = :profileId")
    suspend fun deleteProfile(profileId: String)

    @Query("DELETE FROM profile_fact")
    suspend fun deleteAll()
}
