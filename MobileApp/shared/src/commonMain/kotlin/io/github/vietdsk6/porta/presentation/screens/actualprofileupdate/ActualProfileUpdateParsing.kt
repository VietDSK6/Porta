package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.DurationUnit
import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.util.file.EvidenceAttachmentPolicy
import io.github.vietdsk6.porta.util.file.SelectedEvidenceFile
import kotlinx.datetime.LocalDate

internal fun ProfileSnapshot.evidenceFor(target: ActionTarget): EvidenceMetadata? = when (target) {
    is ActionTarget.ProfileFactTarget -> facts[target.factKey]?.evidence

    is ActionTarget.OpportunityRuleTarget -> ruleAssertions[
        RuleRef(target.opportunityId, target.ruleId),
    ]?.takeIf { it.ruleFingerprint == target.ruleFingerprint }?.evidence
}

internal fun SelectedEvidenceFile.toMetadata() = EvidenceAttachmentMetadata(
    displayName = EvidenceAttachmentPolicy.normalizedDisplayName(displayName),
    mimeType = mimeType,
    byteSize = bytes.size.toLong(),
    storageKey = null,
)

internal fun SelectedEvidenceFile.validationError(): String? = EvidenceAttachmentPolicy.validate(this).exceptionOrNull()?.message

internal fun Double.toInputText(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

internal fun String.toFiniteDoubleOrNull(): Double? = trim().toDoubleOrNull()?.takeIf { it.isFinite() }

internal fun String.toLocalDateOrNull(): LocalDate? {
    val value = trim()
    if (!ISO_LOCAL_DATE.matches(value)) return null
    return runCatching { LocalDate.parse(value) }.getOrNull()
}

internal fun String.toEnumValuesOrNull(): Set<String>? {
    val commaSeparated = split(',')
    if (commaSeparated.isEmpty() || commaSeparated.any(String::isBlank)) return null
    return commaSeparated
        .flatMap { value -> value.lines() }
        .mapNotNull { value -> value.trim().takeIf(String::isNotEmpty) }
        .toCollection(linkedSetOf())
        .takeIf { it.isNotEmpty() }
}

internal fun FactValue.inputUnitLabel(): String = when (this) {
    is FactValue.NumberValue -> unit
    is FactValue.DurationValue -> unit.toInputLabel()
    else -> ""
}

internal fun DurationUnit.toInputLabel(): String = name.lowercase()

private val ISO_LOCAL_DATE = Regex("\\d{4}-\\d{2}-\\d{2}")

internal data class FormState(
    val planId: String? = null,
    val inputKind: ActualValueInputKind = ActualValueInputKind.NUMBER,
    val achievedValue: String = "",
    val achievedBoolean: Boolean? = null,
    val includeEvidence: Boolean = false,
    val evidenceLabel: String = "",
    val evidenceReference: String = "",
    val evidenceAttachment: EvidenceAttachmentMetadata? = null,
    val pendingAttachment: SelectedEvidenceFile? = null,
    val evidenceAttachmentError: String? = null,
    val evidenceAttachmentErrorCode: ActualCopyCode? = null,
    val evidenceLabelTouched: Boolean = false,
    val submitted: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val errorCode: ActualCopyCode? = null,
) {
    companion object {
        fun from(
            plan: PlanItem,
            existingEvidence: EvidenceMetadata?,
        ): FormState = FormState(
            planId = plan.id,
            inputKind = when (plan.targetValue) {
                is FactValue.BooleanValue -> ActualValueInputKind.BOOLEAN
                is FactValue.NumberValue -> ActualValueInputKind.NUMBER
                is FactValue.EnumValue -> ActualValueInputKind.ENUM
                is FactValue.EnumListValue -> ActualValueInputKind.ENUM_LIST
                is FactValue.TextValue -> ActualValueInputKind.TEXT
                is FactValue.DateValue -> ActualValueInputKind.DATE
                is FactValue.DurationValue -> ActualValueInputKind.DURATION
            },
            includeEvidence = plan.target is ActionTarget.OpportunityRuleTarget || existingEvidence != null,
            evidenceLabel = existingEvidence?.label.orEmpty(),
            evidenceReference = existingEvidence?.reference.orEmpty(),
            evidenceAttachment = existingEvidence?.attachment,
        )
    }
}
