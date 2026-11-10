package io.github.vietdsk6.porta.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import io.github.vietdsk6.porta.data.source.local.entity.ProfileFactEntity
import io.github.vietdsk6.porta.data.source.local.entity.RuleAssertionEntity

@Dao
abstract class ProfileImportDao {
    @Query("DELETE FROM profile_fact WHERE profile_id = :profileId")
    protected abstract suspend fun deleteFacts(profileId: String)

    @Query("DELETE FROM rule_assertion WHERE profile_id = :profileId")
    protected abstract suspend fun deleteAssertions(profileId: String)

    @Upsert
    protected abstract suspend fun upsertFacts(entities: List<ProfileFactEntity>)

    @Upsert
    protected abstract suspend fun upsertAssertions(entities: List<RuleAssertionEntity>)

    @Transaction
    open suspend fun replaceProfile(
        profileId: String,
        facts: List<ProfileFactEntity>,
        assertions: List<RuleAssertionEntity>,
    ) {
        require(facts.all { fact -> fact.profileId == profileId }) {
            "Imported facts must target the profile being replaced."
        }
        require(assertions.all { assertion -> assertion.profileId == profileId }) {
            "Imported assertions must target the profile being replaced."
        }
        require(facts.map { fact -> fact.factKey }.distinct().size == facts.size) {
            "Imported facts must have unique keys."
        }
        require(
            assertions
                .map { assertion -> assertion.opportunityId to assertion.ruleId }
                .distinct()
                .size == assertions.size,
        ) {
            "Imported assertions must have unique rule references."
        }
        deleteAssertions(profileId)
        deleteFacts(profileId)
        if (facts.isNotEmpty()) upsertFacts(facts)
        if (assertions.isNotEmpty()) upsertAssertions(assertions)
    }
}
