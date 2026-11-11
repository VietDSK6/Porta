@file:OptIn(ExperimentalUuidApi::class)

package io.github.vietdsk6.porta.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import io.github.vietdsk6.porta.domain.model.ActionTarget
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
    @ColumnInfo(
        name = "target_scope",
        defaultValue = "'PROFILE_FACT'",
    )
    val targetScope: String = TARGET_SCOPE_PROFILE_FACT,
    @ColumnInfo("opportunity_id") val opportunityId: String? = null,
    @ColumnInfo("rule_id") val ruleId: String? = null,
    @ColumnInfo("rule_fingerprint") val ruleFingerprint: String? = null,
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
    @ColumnInfo("route_id") val routeId: String? = null,
    @ColumnInfo("sequence") val sequence: Int? = null,
)

fun PlanItemEntity.toModel(): PlanItem = PlanItem(
    id = id,
    profileId = profileId,
    actionTemplateId = actionTemplateId,
    title = title,
    target = toActionTarget(),
    status = PlanStatus.valueOf(status),
    targetDateEpochDays = targetDateEpochDays,
    note = note,
    expectedDurationDays = expectedDurationDays,
    effort = EffortLevel.valueOf(effort),
    costBand = CostBand.valueOf(costBand),
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    routeId = routeId,
    sequence = sequence,
)

fun PlanItem.toEntity(): PlanItemEntity {
    val storedValue = targetValue.toStored()
    return PlanItemEntity(
        id = id,
        profileId = profileId,
        actionTemplateId = actionTemplateId,
        title = title,
        targetScope = when (target) {
            is ActionTarget.ProfileFactTarget -> TARGET_SCOPE_PROFILE_FACT
            is ActionTarget.OpportunityRuleTarget -> TARGET_SCOPE_OPPORTUNITY_RULE
        },
        opportunityId = (target as? ActionTarget.OpportunityRuleTarget)?.opportunityId,
        ruleId = (target as? ActionTarget.OpportunityRuleTarget)?.ruleId,
        ruleFingerprint = (target as? ActionTarget.OpportunityRuleTarget)?.ruleFingerprint,
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
        routeId = routeId,
        sequence = sequence,
    )
}

private fun PlanItemEntity.toActionTarget(): ActionTarget {
    val factKey = FactKey.valueOf(targetFactKey)
    val factValue = StoredFactValue(
        type = targetValueType,
        number = targetNumberValue,
        text = targetTextValue,
        boolean = targetBooleanValue,
        listJson = targetListValueJson,
        unit = targetUnit,
        durationAmount = targetDurationAmount,
    ).toDomain()
    return when (targetScope) {
        TARGET_SCOPE_PROFILE_FACT -> ActionTarget.ProfileFactTarget(
            factKey = factKey,
            value = factValue,
        )

        TARGET_SCOPE_OPPORTUNITY_RULE -> ActionTarget.OpportunityRuleTarget(
            opportunityId = requireNotNull(opportunityId) {
                "Rule-scoped plan item $id is missing opportunity_id."
            },
            ruleId = requireNotNull(ruleId) {
                "Rule-scoped plan item $id is missing rule_id."
            },
            ruleFingerprint = requireNotNull(ruleFingerprint) {
                "Rule-scoped plan item $id is missing rule_fingerprint."
            },
            factKey = factKey,
            value = factValue,
        )

        else -> error("Unsupported plan item target scope: $targetScope")
    }
}

private const val TARGET_SCOPE_PROFILE_FACT = "PROFILE_FACT"
private const val TARGET_SCOPE_OPPORTUNITY_RULE = "OPPORTUNITY_RULE"
