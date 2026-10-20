@file:OptIn(ExperimentalUuidApi::class)

package io.github.vietdsk6.porta.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import io.github.vietdsk6.porta.domain.model.OpportunityPack
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "opportunity_pack")
data class OpportunityPackEntity(
    @PrimaryKey @ColumnInfo("id") val id: String = Uuid.random().toString(),
    @ColumnInfo("pack_id") val packId: String,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("publisher") val publisher: String,
    @ColumnInfo("schema_version") val schemaVersion: Int,
    @ColumnInfo("pack_version") val version: Int,
    @ColumnInfo("raw_json") val rawJson: String,
    @ColumnInfo("source_url") val sourceUrl: String? = null,
    @ColumnInfo("last_checked_on") val lastCheckedOn: String,
    @ColumnInfo("imported_at") val importedAtEpochMillis: Long,
    @ColumnInfo("is_active") val isActive: Boolean,
)

fun OpportunityPackEntity.toModel(): OpportunityPack = OpportunityPack(
    id = packId,
    name = name,
    publisher = publisher,
    schemaVersion = schemaVersion,
    version = version,
    rawJson = rawJson,
    sourceUrl = sourceUrl,
    lastCheckedOn = LocalDate.parse(lastCheckedOn),
    importedAtEpochMillis = importedAtEpochMillis,
    isActive = isActive,
)

fun OpportunityPack.toEntity(): OpportunityPackEntity = OpportunityPackEntity(
    id = "$id:$version",
    packId = id,
    name = name,
    publisher = publisher,
    schemaVersion = schemaVersion,
    version = version,
    rawJson = rawJson,
    sourceUrl = sourceUrl,
    lastCheckedOn = lastCheckedOn.toString(),
    importedAtEpochMillis = importedAtEpochMillis,
    isActive = isActive,
)
