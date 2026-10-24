package io.github.vietdsk6.porta.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef

@Entity(
    tableName = "rule_assertion",
    primaryKeys = ["profile_id", "opportunity_id", "rule_id"],
)
data class RuleAssertionEntity(
    @ColumnInfo("profile_id") val profileId: String,
    @ColumnInfo("opportunity_id") val opportunityId: String,
    @ColumnInfo("rule_id") val ruleId: String,
    @ColumnInfo("rule_fingerprint") val ruleFingerprint: String,
    @ColumnInfo("fact_key") val factKey: String,
    @ColumnInfo("value_type") val valueType: String,
    @ColumnInfo("number_value") val numberValue: Double? = null,
    @ColumnInfo("text_value") val textValue: String? = null,
    @ColumnInfo("boolean_value") val booleanValue: Boolean? = null,
    @ColumnInfo("list_value_json") val listValueJson: String? = null,
    @ColumnInfo("unit") val unit: String? = null,
    @ColumnInfo("duration_amount") val durationAmount: Int? = null,
    @ColumnInfo("evidence_label") val evidenceLabel: String? = null,
    @ColumnInfo("evidence_reference") val evidenceReference: String? = null,
    @ColumnInfo("evidence_checked_at") val evidenceCheckedAtEpochMillis: Long? = null,
    @ColumnInfo("updated_at") val updatedAtEpochMillis: Long,
)

fun RuleAssertionEntity.toModel(): RuleAssertion = RuleAssertion(
    profileId = profileId,
    ruleRef = RuleRef(
        opportunityId = opportunityId,
        ruleId = ruleId,
    ),
    ruleFingerprint = ruleFingerprint,
    factKey = FactKey.valueOf(factKey),
    value = StoredFactValue(
        type = valueType,
        number = numberValue,
        text = textValue,
        boolean = booleanValue,
        listJson = listValueJson,
        unit = unit,
        durationAmount = durationAmount,
    ).toDomain(),
    evidence = evidenceLabel?.let {
        EvidenceMetadata(
            label = it,
            reference = evidenceReference,
            checkedAtEpochMillis = evidenceCheckedAtEpochMillis,
        )
    },
    updatedAtEpochMillis = updatedAtEpochMillis,
)

fun RuleAssertion.toEntity(): RuleAssertionEntity {
    val storedValue = value.toStored()
    return RuleAssertionEntity(
        profileId = profileId,
        opportunityId = ruleRef.opportunityId,
        ruleId = ruleRef.ruleId,
        ruleFingerprint = ruleFingerprint,
        factKey = factKey.name,
        valueType = storedValue.type,
        numberValue = storedValue.number,
        textValue = storedValue.text,
        booleanValue = storedValue.boolean,
        listValueJson = storedValue.listJson,
        unit = storedValue.unit,
        durationAmount = storedValue.durationAmount,
        evidenceLabel = evidence?.label,
        evidenceReference = evidence?.reference,
        evidenceCheckedAtEpochMillis = evidence?.checkedAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
}
