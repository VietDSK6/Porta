package io.github.vietdsk6.porta.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import io.github.vietdsk6.porta.data.source.local.entity.PlanItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanItemDao {
    @Query("SELECT * FROM plan_item WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PlanItemEntity?

    @Query(
        "SELECT * FROM plan_item WHERE profile_id = :profileId " +
            "ORDER BY status ASC, target_date_epoch_days ASC, created_at DESC",
    )
    fun observeForProfile(profileId: String): Flow<List<PlanItemEntity>>

    @Query(
        "SELECT * FROM plan_item WHERE profile_id = :profileId AND status = :status " +
            "ORDER BY target_date_epoch_days ASC, created_at DESC",
    )
    fun observeByStatus(profileId: String, status: String): Flow<List<PlanItemEntity>>

    @Upsert
    suspend fun upsert(entity: PlanItemEntity)

    @Transaction
    suspend fun replaceForProfile(entity: PlanItemEntity) {
        deleteForProfile(entity.profileId)
        upsert(entity)
    }

    @Query("UPDATE plan_item SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query("DELETE FROM plan_item WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM plan_item WHERE profile_id = :profileId")
    suspend fun deleteForProfile(profileId: String)
}
