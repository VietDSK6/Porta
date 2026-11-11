package io.github.vietdsk6.porta.domain.model

/**
 * A stable fingerprint of the atomic rule semantics used to invalidate scoped assertions when a
 * curated opportunity pack changes the requirement while retaining the same rule id.
 */
fun RuleNode.Atomic.fingerprint(): String {
    val canonical = listOf(
        factKey.name,
        operator.name,
        expected.canonicalValue(),
        explanation,
        sourceNote.orEmpty(),
        actionability.name,
        actionTemplateId.orEmpty(),
    ).joinToString(separator = "|") { value -> "${value.length}:$value" }
    return "atomic-v1-${canonical.fnv1a64()}"
}

fun RuleNode.findAtomicRule(ruleId: String): RuleNode.Atomic? = when (this) {
    is RuleNode.All -> children.firstNotNullOfOrNull { child -> child.findAtomicRule(ruleId) }
    is RuleNode.Any -> children.firstNotNullOfOrNull { child -> child.findAtomicRule(ruleId) }
    is RuleNode.Atomic -> takeIf { id == ruleId }
    is RuleNode.ManualCheck -> null
}

private fun FactValue?.canonicalValue(): String = when (this) {
    null -> "null"

    is FactValue.BooleanValue -> "boolean:$value"

    is FactValue.DateValue -> "date:$value"

    is FactValue.DurationValue -> "duration:$amount:${unit.name}"

    is FactValue.EnumListValue -> values.sorted().joinToString(prefix = "enum-list:[", postfix = "]") { value ->
        "${value.length}:$value"
    }

    is FactValue.EnumValue -> "enum:${value.length}:$value"

    is FactValue.NumberValue -> "number:$amount:${unit.length}:$unit"

    is FactValue.TextValue -> "text:${value.length}:$value"
}

private fun String.fnv1a64(): String {
    var hash = -3750763034362895579L
    forEach { character ->
        hash = hash xor character.code.toLong()
        hash *= 1099511628211L
    }
    return hash.toULong().toString(radix = 16).padStart(length = 16, padChar = '0')
}
