@file:OptIn(ExperimentalUuidApi::class)

package io.github.vietdsk6.porta.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import io.github.vietdsk6.porta.domain.model.SavedOpportunity
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "saved_opportunity",
    indices = [Index(value = ["profile_id", "opportunity_id"], unique = true)],
)
data class SavedOpportunityEntity(
    @PrimaryKey @ColumnInfo("id") val id: String = Uuid.random().toString(),
    @ColumnInfo("profile_id") val profileId: String,
    @ColumnInfo("opportunity_id") val opportunityId: String,
    @ColumnInfo("saved_at") val savedAtEpochMillis: Long,
)

fun SavedOpportunityEntity.toModel(): SavedOpportunity = SavedOpportunity(
    id = id,
    profileId = profileId,
    opportunityId = opportunityId,
    savedAtEpochMillis = savedAtEpochMillis,
)

fun SavedOpportunity.toEntity(): SavedOpportunityEntity = SavedOpportunityEntity(
    id = id,
    profileId = profileId,
    opportunityId = opportunityId,
    savedAtEpochMillis = savedAtEpochMillis,
)
