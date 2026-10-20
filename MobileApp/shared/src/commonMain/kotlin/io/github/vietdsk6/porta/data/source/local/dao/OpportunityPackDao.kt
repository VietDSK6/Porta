package io.github.vietdsk6.porta.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import io.github.vietdsk6.porta.data.source.local.entity.OpportunityPackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OpportunityPackDao {
    @Query("SELECT * FROM opportunity_pack WHERE is_active = 1 ORDER BY imported_at DESC LIMIT 1")
    fun observeActive(): Flow<OpportunityPackEntity?>

    @Query("SELECT * FROM opportunity_pack WHERE is_active = 1 ORDER BY imported_at DESC LIMIT 1")
    suspend fun getActive(): OpportunityPackEntity?

    @Query(
        "SELECT * FROM opportunity_pack " +
            "WHERE pack_id = :packId AND pack_version = :version LIMIT 1",
    )
    suspend fun getByPackIdAndVersion(
        packId: String,
        version: Int,
    ): OpportunityPackEntity?

    @Query("SELECT * FROM opportunity_pack ORDER BY imported_at DESC")
    fun observeAll(): Flow<List<OpportunityPackEntity>>

    @Upsert
    suspend fun upsert(entity: OpportunityPackEntity)

    @Query("UPDATE opportunity_pack SET is_active = 0 WHERE is_active = 1")
    suspend fun deactivateAll()

    @Transaction
    suspend fun replaceActive(entity: OpportunityPackEntity) {
        deactivateAll()
        upsert(entity.copy(isActive = true))
    }

    @Query("DELETE FROM opportunity_pack WHERE id = :id")
    suspend fun deleteById(id: String)
}
