@file:OptIn(ExperimentalUuidApi::class)

package io.github.vietdsk6.porta.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "plan_item")
data class PlanItemEntity(
    @PrimaryKey @ColumnInfo("id") val id: String = Uuid.random().toString(),
    @ColumnInfo("profile_id") val profileId: String,
    @ColumnInfo("action_template_id") val actionTemplateId: String,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("target_fact_key") val targetFactKey: String,
    @ColumnInfo("target_value_type") val targetValueType: String,
    @ColumnInfo("target_number_value") val targetNumberValue: Double? = null,
    @ColumnInfo("target_text_value") val targetTextValue: String? = null,
    @ColumnInfo("target_boolean_value") val targetBooleanValue: Boolean? = null,
    @ColumnInfo("target_list_value_json") val targetListValueJson: String? = null,
    @ColumnInfo("target_unit") val targetUnit: String? = null,
    @ColumnInfo("target_duration_amount") val targetDurationAmount: Int? = null,
    @ColumnInfo("status") val status: String,
    @ColumnInfo("target_date_epoch_days") val targetDateEpochDays: Long? = null,
    @ColumnInfo("note") val note: String? = null,
    @ColumnInfo("expected_duration_days") val expectedDurationDays: Int? = null,
    @ColumnInfo("effort") val effort: String,
    @ColumnInfo("cost_band") val costBand: String,
    @ColumnInfo("created_at") val createdAtEpochMillis: Long,
    @ColumnInfo("updated_at") val updatedAtEpochMillis: Long,
)

fun PlanItemEntity.toModel(): PlanItem = PlanItem(
    id = id,
    profileId = profileId,
    actionTemplateId = actionTemplateId,
    title = title,
    targetFactKey = FactKey.valueOf(targetFactKey),
    targetValue = StoredFactValue(
        type = targetValueType,
        number = targetNumberValue,
        text = targetTextValue,
        boolean = targetBooleanValue,
        listJson = targetListValueJson,
        unit = targetUnit,
        durationAmount = targetDurationAmount,
    ).toDomain(),
    status = PlanStatus.valueOf(status),
    targetDateEpochDays = targetDateEpochDays,
    note = note,
    expectedDurationDays = expectedDurationDays,
    effort = EffortLevel.valueOf(effort),
    costBand = CostBand.valueOf(costBand),
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

fun PlanItem.toEntity(): PlanItemEntity {
    val storedValue = targetValue.toStored()
    return PlanItemEntity(
        id = id,
        profileId = profileId,
        actionTemplateId = actionTemplateId,
        title = title,
        targetFactKey = targetFactKey.name,
        targetValueType = storedValue.type,
        targetNumberValue = storedValue.number,
        targetTextValue = storedValue.text,
        targetBooleanValue = storedValue.boolean,
        targetListValueJson = storedValue.listJson,
        targetUnit = storedValue.unit,
        targetDurationAmount = storedValue.durationAmount,
        status = status.name,
        targetDateEpochDays = targetDateEpochDays,
        note = note,
        expectedDurationDays = expectedDurationDays,
        effort = effort.name,
        costBand = costBand.name,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
}
