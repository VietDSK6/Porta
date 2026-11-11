package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate

/** A model suggestion remains inert until its quote is found and a curator accepts it. */
data class RuleDraftSuggestion(
    val id: String,
    val kind: DraftRuleKind,
    val factKey: FactKey? = null,
    val operator: Operator? = null,
    val expected: FactValue? = null,
    val actionability: Actionability = Actionability.UNKNOWN,
    val quote: String,
    val rationale: String,
    val groundingStatus: DraftGroundingStatus = DraftGroundingStatus.UNCHECKED,
    val sourceRange: SourceTextRange? = null,
    val reviewStatus: DraftReviewStatus = DraftReviewStatus.PENDING,
    val manualFallbackReason: String? = null,
) {
    val canAccept: Boolean
        get() = groundingStatus == DraftGroundingStatus.GROUNDED && reviewStatus == DraftReviewStatus.PENDING
}

enum class DraftRuleKind {
    ATOMIC,
    MANUAL_CHECK,
}

enum class DraftGroundingStatus {
    UNCHECKED,
    GROUNDED,
    REJECTED_UNGROUNDED,
}

enum class DraftReviewStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}

/** UTF-16 offsets into the exact pasted source, with [endExclusive] suitable for text highlighting. */
data class SourceTextRange(val start: Int, val endExclusive: Int)

/** Transport-neutral model output. These fields are strings until validated against Porta's closed catalog. */
data class RawRuleDraftSuggestion(
    val id: String,
    val kind: String,
    val factKey: String? = null,
    val operator: String? = null,
    val expected: DraftExpectedValue? = null,
    val actionability: String? = null,
    val quote: String,
    val rationale: String,
)

data class DraftExpectedValue(
    val kind: String,
    val value: String,
    val unit: String? = null,
)

/** Unknown facts, operators, value kinds, units, and incompatible predicates become manual checks. */
fun RawRuleDraftSuggestion.toSafeSuggestion(): RuleDraftSuggestion {
    fun manual(reason: String): RuleDraftSuggestion = RuleDraftSuggestion(
        id = id,
        kind = DraftRuleKind.MANUAL_CHECK,
        quote = quote,
        rationale = rationale,
        manualFallbackReason = reason,
    )

    val parsedKind = if (kind.trim().equals("manual", ignoreCase = true)) {
        DraftRuleKind.MANUAL_CHECK
    } else {
        enumOrNull<DraftRuleKind>(kind)
    }
        ?: return manual("Unsupported rule kind '$kind'.")
    if (parsedKind == DraftRuleKind.MANUAL_CHECK) {
        return RuleDraftSuggestion(id = id, kind = parsedKind, quote = quote, rationale = rationale)
    }

    val key = factKey?.let { enumOrNull<FactKey>(it) }
        ?: return manual("Unsupported or missing fact key '${factKey.orEmpty()}'.")
    val parsedOperator = operator?.let { enumOrNull<Operator>(it) }
        ?: return manual("Unsupported or missing operator '${operator.orEmpty()}'.")
    val parsedActionability = actionability?.let { enumOrNull<Actionability>(it) } ?: Actionability.UNKNOWN
    if (actionability != null && parsedActionability == Actionability.UNKNOWN && !actionability.equals("UNKNOWN", true)) {
        return manual("Unsupported actionability '$actionability'.")
    }
    val parsedExpected = if (parsedOperator == Operator.EXISTS) {
        if (expected != null) return manual("EXISTS must not have an expected value.")
        null
    } else {
        expected?.toFactValue() ?: return manual("Unsupported or missing expected value.")
    }
    if (!isSupportedPredicate(key, parsedOperator, parsedExpected)) {
        return manual("The fact, operator, value, or unit combination is not supported by Porta.")
    }
    return RuleDraftSuggestion(
        id = id,
        kind = DraftRuleKind.ATOMIC,
        factKey = key,
        operator = parsedOperator,
        expected = parsedExpected,
        actionability = parsedActionability,
        quote = quote,
        rationale = rationale,
    )
}

private inline fun <reified T : Enum<T>> enumOrNull(raw: String): T? = enumValues<T>().firstOrNull { it.name.equals(raw.trim().replace('-', '_'), ignoreCase = true) }

private fun DraftExpectedValue.toFactValue(): FactValue? = when (kind.trim().lowercase()) {
    "number" -> value.toDoubleOrNull()?.takeIf { it.isFinite() }?.let { amount ->
        unit?.trim()?.takeIf { it.isNotEmpty() }?.let { FactValue.NumberValue(amount, it) }
    }

    "boolean" -> when (value.trim().lowercase()) {
        "true" -> FactValue.BooleanValue(true)
        "false" -> FactValue.BooleanValue(false)
        else -> null
    }

    "enum" -> value.trim().takeIf { it.isNotEmpty() }?.let { FactValue.EnumValue(it) }

    "enum_list" -> value.split(',').map { it.trim() }.takeIf { it.isNotEmpty() && it.all { item -> item.isNotEmpty() } }
        ?.let { FactValue.EnumListValue(it.toSet()) }

    "date" -> runCatching { FactValue.DateValue(LocalDate.parse(value.trim())) }.getOrNull()

    "duration" -> value.toIntOrNull()?.takeIf { it >= 0 }?.let { amount ->
        unit?.let { enumOrNull<DurationUnit>(it) }?.let { FactValue.DurationValue(amount, it) }
    }

    "text" -> value.trim().takeIf { it.isNotEmpty() }?.let { FactValue.TextValue(it) }

    else -> null
}

private fun isSupportedPredicate(key: FactKey, operator: Operator, expected: FactValue?): Boolean {
    if (operator == Operator.EXISTS) return expected == null
    return when (key) {
        FactKey.GPA, FactKey.IELTS_SCORE, FactKey.EXPERIENCE_MONTHS, FactKey.AGE -> {
            val number = expected as? FactValue.NumberValue ?: return false
            val units = when (key) {
                FactKey.GPA -> setOf("4.0 scale", "10.0 scale")
                FactKey.IELTS_SCORE -> setOf("band")
                FactKey.EXPERIENCE_MONTHS -> setOf("months")
                FactKey.AGE -> setOf("years")
                else -> emptySet()
            }
            number.unit in units && operator in setOf(
                Operator.EQUALS,
                Operator.NOT_EQUALS,
                Operator.GREATER_THAN_OR_EQUAL,
                Operator.LESS_THAN_OR_EQUAL,
            )
        }

        FactKey.ENROLLED, FactKey.DEGREE_COMPLETED, FactKey.COMMUNITY_PROJECT,
        FactKey.LEADERSHIP, FactKey.AVAILABLE_FULL_PERIOD,
        -> expected is FactValue.BooleanValue && operator in setOf(Operator.EQUALS, Operator.NOT_EQUALS)

        FactKey.STUDY_LEVEL, FactKey.NATIONALITY, FactKey.RESIDENCY -> when (operator) {
            Operator.EQUALS, Operator.NOT_EQUALS -> expected is FactValue.EnumValue
            Operator.IN_SET -> expected is FactValue.EnumListValue
            else -> false
        }

        FactKey.FIELD_OF_STUDY -> when (operator) {
            Operator.EQUALS, Operator.NOT_EQUALS -> expected is FactValue.TextValue
            Operator.IN_SET -> expected is FactValue.EnumListValue
            else -> false
        }

        FactKey.DATE_OF_BIRTH -> expected is FactValue.DateValue && operator in setOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.DATE_BEFORE,
            Operator.DATE_AFTER,
        )
    }
}
