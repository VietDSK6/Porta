package io.github.vietdsk6.porta.presentation.screens.studentprofile

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.presentation.locale.displayValue
import io.github.vietdsk6.porta.presentation.screens.profilesetup.countryForStoredValue

internal fun RuleNode.factKeys(): Set<FactKey> = when (this) {
    is RuleNode.All -> children.flatMapTo(mutableSetOf()) { it.factKeys() }
    is RuleNode.Any -> children.flatMapTo(mutableSetOf()) { it.factKeys() }
    is RuleNode.Atomic -> setOf(factKey)
    is RuleNode.ManualCheck -> emptySet()
}

internal fun FactValue.profileDisplayValue(key: FactKey): String = when {
    key == FactKey.GPA && this is FactValue.NumberValue -> {
        val scale = unit.removeSuffix(" scale").ifBlank { "4.0" }
        "${amount.compact()} / $scale"
    }

    (key == FactKey.NATIONALITY || key == FactKey.RESIDENCY) && this is FactValue.EnumValue -> {
        countryForStoredValue(value)?.displayName ?: value
    }

    this is FactValue.NumberValue -> "${amount.compact()} $unit".trim()

    else -> displayValue()
}

internal fun Double.compact(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

internal fun Long.readableFileSize(): String = when {
    this >= PROFILE_BYTES_PER_MEGABYTE -> "${(this.toDouble() / PROFILE_BYTES_PER_MEGABYTE).oneDecimal()} MB"
    this >= PROFILE_BYTES_PER_KILOBYTE -> "${(this.toDouble() / PROFILE_BYTES_PER_KILOBYTE).oneDecimal()} KB"
    else -> "$this B"
}

internal fun Double.oneDecimal(): String {
    val roundedTenths = (this * 10.0).toInt()
    val whole = roundedTenths / 10
    val fraction = roundedTenths % 10
    return if (fraction == 0) whole.toString() else "$whole.$fraction"
}

internal fun FactKey.label(): String = when (this) {
    FactKey.GPA -> "GPA"
    FactKey.STUDY_LEVEL -> "Study level"
    FactKey.FIELD_OF_STUDY -> "Field of study"
    FactKey.NATIONALITY -> "Nationality"
    FactKey.RESIDENCY -> "Residency"
    FactKey.IELTS_SCORE -> "IELTS score"
    FactKey.EXPERIENCE_MONTHS -> "Experience"
    FactKey.COMMUNITY_PROJECT -> "Community project"
    FactKey.LEADERSHIP -> "Leadership"
    FactKey.ENROLLED -> "Currently enrolled"
    FactKey.AGE -> "Age"
    FactKey.DATE_OF_BIRTH -> "Date of birth"
    FactKey.DEGREE_COMPLETED -> "Degree completed"
    FactKey.AVAILABLE_FULL_PERIOD -> "Available for full period"
}

internal fun FactProvenance.label(): String = when (this) {
    FactProvenance.SELF_REPORTED -> "Self-reported"
    FactProvenance.EVIDENCE_BACKED -> "Evidence-backed"
}

internal fun Actionability.label(): String = when (this) {
    Actionability.CONTROLLABLE -> "Can change"
    Actionability.TIME_DEPENDENT -> "Changes with time"
    Actionability.CONTEXT_ONLY -> "Context only"
    Actionability.IMMUTABLE -> "Not an action target"
    Actionability.UNKNOWN -> "Actionability unknown"
}

private const val PROFILE_BYTES_PER_KILOBYTE = 1_024L
private const val PROFILE_BYTES_PER_MEGABYTE = 1_024L * 1_024L
