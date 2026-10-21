package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate

data class ProfileFact(
    val id: String,
    val profileId: String = DEFAULT_PROFILE_ID,
    val key: FactKey,
    val value: FactValue,
    val provenance: FactProvenance = FactProvenance.SELF_REPORTED,
    val actionability: Actionability = Actionability.UNKNOWN,
    val evidence: EvidenceMetadata? = null,
    val updatedAtEpochMillis: Long = 0L,
) {
    companion object {
        const val DEFAULT_PROFILE_ID = "default"
    }
}

data class ProfileSnapshot(
    val id: String = ProfileFact.DEFAULT_PROFILE_ID,
    val facts: Map<FactKey, ProfileFact> = emptyMap(),
) {
    fun withOverrides(overrides: Map<FactKey, FactValue>): ProfileSnapshot = copy(
        facts = facts + overrides.mapValues { (key, value) ->
            facts[key]?.copy(value = value) ?: ProfileFact(
                id = "$id:${key.name.lowercase()}",
                profileId = id,
                key = key,
                value = value,
                actionability = Actionability.CONTROLLABLE,
            )
        },
    )
}

enum class FactKey {
    GPA,
    STUDY_LEVEL,
    FIELD_OF_STUDY,
    NATIONALITY,
    RESIDENCY,
    IELTS_SCORE,
    EXPERIENCE_MONTHS,
    COMMUNITY_PROJECT,
    LEADERSHIP,
    ENROLLED,
    AGE,
    DEGREE_COMPLETED,
    AVAILABLE_FULL_PERIOD,
}

sealed interface FactValue {
    data class NumberValue(val amount: Double, val unit: String) : FactValue

    data class BooleanValue(val value: Boolean) : FactValue

    data class EnumValue(val value: String) : FactValue

    data class EnumListValue(val values: Set<String>) : FactValue

    data class DateValue(val value: LocalDate) : FactValue

    data class DurationValue(val amount: Int, val unit: DurationUnit) : FactValue

    data class TextValue(val value: String) : FactValue
}

enum class DurationUnit {
    HOURS,
    DAYS,
    MONTHS,
    YEARS,
}

enum class FactProvenance {
    SELF_REPORTED,
    EVIDENCE_BACKED,
}

enum class Actionability {
    CONTROLLABLE,
    TIME_DEPENDENT,
    CONTEXT_ONLY,
    IMMUTABLE,
    UNKNOWN,
}

data class EvidenceMetadata(
    val label: String,
    val reference: String? = null,
    val checkedAtEpochMillis: Long? = null,
)

fun FactValue.displayValue(): String = when (this) {
    is FactValue.BooleanValue -> if (value) "Yes" else "No"

    is FactValue.DateValue -> value.toString()

    is FactValue.DurationValue -> "$amount ${unit.name.lowercase()}"

    is FactValue.EnumListValue -> values.sorted().joinToString()

    is FactValue.EnumValue -> value

    is FactValue.NumberValue -> if (unit.endsWith(" scale")) {
        "$amount / ${unit.removeSuffix(" scale")}"
    } else {
        "$amount $unit".trim()
    }

    is FactValue.TextValue -> value
}
