@file:OptIn(ExperimentalUuidApi::class)

package io.github.vietdsk6.porta.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.ProfileFact
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "profile_fact",
    indices = [Index(value = ["profile_id", "fact_key"], unique = true)],
)
data class ProfileFactEntity(
    @PrimaryKey @ColumnInfo("id") val id: String = Uuid.random().toString(),
    @ColumnInfo("profile_id") val profileId: String,
    @ColumnInfo("fact_key") val factKey: String,
    @ColumnInfo("value_type") val valueType: String,
    @ColumnInfo("number_value") val numberValue: Double? = null,
    @ColumnInfo("text_value") val textValue: String? = null,
    @ColumnInfo("boolean_value") val booleanValue: Boolean? = null,
    @ColumnInfo("list_value_json") val listValueJson: String? = null,
    @ColumnInfo("unit") val unit: String? = null,
    @ColumnInfo("duration_amount") val durationAmount: Int? = null,
    @ColumnInfo("provenance") val provenance: String,
    @ColumnInfo("actionability") val actionability: String,
    @ColumnInfo("evidence_label") val evidenceLabel: String? = null,
    @ColumnInfo("evidence_reference") val evidenceReference: String? = null,
    @ColumnInfo("evidence_checked_at") val evidenceCheckedAtEpochMillis: Long? = null,
    @ColumnInfo("updated_at") val updatedAtEpochMillis: Long,
)

fun ProfileFactEntity.toModel(): ProfileFact = ProfileFact(
    id = id,
    profileId = profileId,
    key = FactKey.valueOf(factKey),
    value = StoredFactValue(
        type = valueType,
        number = numberValue,
        text = textValue,
        boolean = booleanValue,
        listJson = listValueJson,
        unit = unit,
        durationAmount = durationAmount,
    ).toDomain(),
    provenance = FactProvenance.valueOf(provenance),
    actionability = Actionability.valueOf(actionability),
    evidence = evidenceLabel?.let {
        EvidenceMetadata(
            label = it,
            reference = evidenceReference,
            checkedAtEpochMillis = evidenceCheckedAtEpochMillis,
        )
    },
    updatedAtEpochMillis = updatedAtEpochMillis,
)

fun ProfileFact.toEntity(): ProfileFactEntity {
    val storedValue = value.toStored()
    return ProfileFactEntity(
        id = id,
        profileId = profileId,
        factKey = key.name,
        valueType = storedValue.type,
        numberValue = storedValue.number,
        textValue = storedValue.text,
        booleanValue = storedValue.boolean,
        listValueJson = storedValue.listJson,
        unit = storedValue.unit,
        durationAmount = storedValue.durationAmount,
        provenance = provenance.name,
        actionability = actionability.name,
        evidenceLabel = evidence?.label,
        evidenceReference = evidence?.reference,
        evidenceCheckedAtEpochMillis = evidence?.checkedAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
}
