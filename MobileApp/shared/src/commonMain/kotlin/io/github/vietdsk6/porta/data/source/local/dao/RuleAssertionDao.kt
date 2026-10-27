package io.github.vietdsk6.porta.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import io.github.vietdsk6.porta.data.source.local.entity.RuleAssertionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleAssertionDao {
    @Query(
        "SELECT * FROM rule_assertion " +
            "WHERE profile_id = :profileId AND opportunity_id = :opportunityId AND rule_id = :ruleId " +
            "LIMIT 1",
    )
    suspend fun get(
        profileId: String,
        opportunityId: String,
        ruleId: String,
    ): RuleAssertionEntity?

    @Query(
        "SELECT * FROM rule_assertion WHERE profile_id = :profileId " +
            "ORDER BY opportunity_id, rule_id",
    )
    fun observeForProfile(profileId: String): Flow<List<RuleAssertionEntity>>

    @Query(
        "SELECT * FROM rule_assertion WHERE profile_id = :profileId " +
            "ORDER BY opportunity_id, rule_id",
    )
    suspend fun getForProfile(profileId: String): List<RuleAssertionEntity>

    @Upsert
    suspend fun upsert(entity: RuleAssertionEntity)

    @Upsert
    suspend fun upsertAll(entities: List<RuleAssertionEntity>)

    @Query(
        "DELETE FROM rule_assertion " +
            "WHERE profile_id = :profileId AND opportunity_id = :opportunityId AND rule_id = :ruleId",
    )
    suspend fun delete(
        profileId: String,
        opportunityId: String,
        ruleId: String,
    )

    @Query("DELETE FROM rule_assertion WHERE profile_id = :profileId")
    suspend fun deleteForProfile(profileId: String)

    @Query("DELETE FROM rule_assertion")
    suspend fun deleteAll()
}
