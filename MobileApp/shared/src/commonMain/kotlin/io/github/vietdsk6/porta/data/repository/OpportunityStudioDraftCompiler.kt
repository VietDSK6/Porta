package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.source.local.pack.DeadlineDto
import io.github.vietdsk6.porta.data.source.local.pack.FactValueDto
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityDto
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.RuleNodeDto
import io.github.vietdsk6.porta.domain.model.DurationUnit
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.OpportunityStudioDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioExpectedValueDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.PackDiagnostic
import io.github.vietdsk6.porta.domain.model.StudioDeadlineKind
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.domain.model.StudioRootRuleKind
import kotlinx.datetime.LocalDate

data class OpportunityStudioCompilation(
    val rawJson: String?,
    val diagnostics: List<PackDiagnostic>,
    val nextPackVersion: Int,
) {
    val isValid: Boolean
        get() = rawJson != null && diagnostics.isEmpty()
}

/**
 * Compiles a Studio draft into a candidate pack without crossing the persistence boundary.
 *
 * The active document is decoded, cloned, versioned, and extended with the authored record. The
 * candidate then passes through the same parser and validator as file imports. Callers may export
 * [OpportunityStudioCompilation.rawJson], but activating it remains an explicit Pack Inspector
 * import-and-approval step.
 */
class OpportunityStudioDraftCompiler(
    private val codec: OpportunityPackCodec,
    private val parser: OpportunityPackParser,
) {
    fun compile(
        base: OpportunityPackDocument,
        draft: OpportunityStudioDraft,
    ): OpportunityStudioCompilation {
        val nextPackVersion = base.metadata.version + 1
        val basePack = codec.decode(base.metadata.rawJson).getOrElse { error ->
            return OpportunityStudioCompilation(
                rawJson = null,
                diagnostics = listOf(
                    PackDiagnostic(
                        code = "invalid_base_pack",
                        message = error.message ?: "The active pack could not be decoded.",
                        path = "$",
                    ),
                ),
                nextPackVersion = nextPackVersion,
            )
        }
        val draftDiagnostics = mutableListOf<PackDiagnostic>()
        val ruleDtos = draft.rules.mapIndexedNotNull { index, rule ->
            rule.toDto(index = index, diagnostics = draftDiagnostics)
        }
        if (draftDiagnostics.isNotEmpty()) {
            return OpportunityStudioCompilation(
                rawJson = null,
                diagnostics = draftDiagnostics,
                nextPackVersion = nextPackVersion,
            )
        }

        val rootId = uniqueRootId(draft.opportunityId, draft.rules.map { it.id.trim() }.toSet())
        val rootRule = when (draft.rootRuleKind) {
            StudioRootRuleKind.ALL -> RuleNodeDto.All(id = rootId, children = ruleDtos)
            StudioRootRuleKind.ANY -> RuleNodeDto.Any(id = rootId, children = ruleDtos)
        }
        val opportunity = OpportunityDto(
            id = draft.opportunityId.trim(),
            title = draft.title.trim(),
            provider = draft.provider.trim(),
            category = draft.category.name,
            cycle = draft.cycle.trim(),
            cycleStartOn = draft.cycleStartOn.trim().takeIf(String::isNotEmpty),
            sourceUrl = draft.sourceUrl.trim(),
            sourceSection = draft.sourceSection.trim().takeIf(String::isNotEmpty),
            lastCheckedOn = draft.lastCheckedOn.trim(),
            deadline = draft.deadlineDto(),
            eligibilityRule = rootRule,
            schemaVersion = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
        )
        val candidate = basePack.copy(
            schemaVersion = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
            version = nextPackVersion,
            lastCheckedOn = draft.lastCheckedOn.trim(),
            opportunities = basePack.opportunities + opportunity,
        )
        val rawJson = codec.encode(candidate)
        val validation = parser.parse(rawJson)
        return OpportunityStudioCompilation(
            rawJson = rawJson.takeIf { validation.isValid },
            diagnostics = validation.diagnostics,
            nextPackVersion = nextPackVersion,
        )
    }

    private fun OpportunityStudioRuleDraft.toDto(
        index: Int,
        diagnostics: MutableList<PackDiagnostic>,
    ): RuleNodeDto? = when (this) {
        is OpportunityStudioRuleDraft.Atomic -> {
            val expectedValue = expected
            val expectedDto = when {
                operator == Operator.EXISTS -> null
                expectedValue == null -> null
                else -> expectedValue.toDto(index = index, diagnostics = diagnostics)
            }
            RuleNodeDto.Atomic(
                id = id.trim(),
                factKey = factKey.name,
                operator = operator.name,
                expected = expectedDto,
                explanation = explanation.trim(),
                sourceNote = sourceNote.trim().takeIf(String::isNotEmpty),
                actionability = actionability.name,
            )
        }

        is OpportunityStudioRuleDraft.ManualCheck -> RuleNodeDto.ManualCheck(
            id = id.trim(),
            explanation = explanation.trim(),
            sourceNote = sourceNote.trim().takeIf(String::isNotEmpty),
        )
    }

    private fun OpportunityStudioExpectedValueDraft.toDto(
        index: Int,
        diagnostics: MutableList<PackDiagnostic>,
    ): FactValueDto? {
        val path = "rules[$index].expected"
        fun invalid(code: String, message: String, field: String = "value"): FactValueDto? {
            diagnostics += PackDiagnostic(code = code, message = message, path = "$path.$field")
            return null
        }
        val trimmedValue = value.trim()
        val trimmedUnit = unit.trim()
        return when (kind) {
            StudioFactValueKind.BOOLEAN -> when (trimmedValue.lowercase()) {
                "true" -> FactValueDto.BooleanValue(true)
                "false" -> FactValueDto.BooleanValue(false)
                else -> invalid("invalid_boolean_value", "Boolean values must be true or false.")
            }

            StudioFactValueKind.NUMBER -> {
                val amount = trimmedValue.toDoubleOrNull()
                when {
                    amount == null || !amount.isFinite() -> invalid(
                        "invalid_number_value",
                        "Number values must be finite numbers.",
                    )

                    trimmedUnit.isEmpty() -> invalid(
                        "missing_numeric_unit",
                        "Number values require a unit or scale.",
                        field = "unit",
                    )

                    else -> FactValueDto.NumberValue(amount = amount, unit = trimmedUnit)
                }
            }

            StudioFactValueKind.ENUM -> if (trimmedValue.isEmpty()) {
                invalid("empty_enum_value", "Enum values cannot be blank.")
            } else {
                FactValueDto.EnumValue(trimmedValue)
            }

            StudioFactValueKind.ENUM_LIST -> {
                val values = trimmedValue
                    .split(',', '\n')
                    .map(String::trim)
                    .filter(String::isNotEmpty)
                    .toSet()
                if (values.isEmpty()) {
                    invalid("empty_enum_list", "Enum-list values require at least one item.")
                } else {
                    FactValueDto.EnumListValue(values)
                }
            }

            StudioFactValueKind.TEXT -> if (trimmedValue.isEmpty()) {
                invalid("empty_text_value", "Text values cannot be blank.")
            } else {
                FactValueDto.TextValue(trimmedValue)
            }

            StudioFactValueKind.DATE -> if (trimmedValue.toLocalDateOrNull() == null) {
                invalid("invalid_date_value", "Date values must use ISO YYYY-MM-DD.")
            } else {
                FactValueDto.DateValue(trimmedValue)
            }

            StudioFactValueKind.DURATION -> {
                val amount = trimmedValue.toIntOrNull()
                val durationUnit = enumValues<DurationUnit>().firstOrNull { it.name == trimmedUnit.uppercase() }
                when {
                    amount == null || amount < 0 -> invalid(
                        "invalid_duration_value",
                        "Duration values must be non-negative whole numbers.",
                    )

                    durationUnit == null -> invalid(
                        "unknown_duration_unit",
                        "Duration unit must be hours, days, months, or years.",
                        field = "unit",
                    )

                    else -> FactValueDto.DurationValue(amount = amount, unit = durationUnit.name)
                }
            }
        }
    }

    private fun OpportunityStudioDraft.deadlineDto(): DeadlineDto = when (deadlineKind) {
        StudioDeadlineKind.KNOWN -> DeadlineDto.Known(deadlineDate.trim())
        StudioDeadlineKind.TBA -> DeadlineDto.Tba
        StudioDeadlineKind.MISSING -> DeadlineDto.Missing
    }

    private fun uniqueRootId(opportunityId: String, childIds: Set<String>): String {
        val prefix = opportunityId.trim().ifEmpty { "studio" }
        val candidate = "$prefix-eligibility-root"
        if (candidate !in childIds) return candidate
        var suffix = 2
        while ("$candidate-$suffix" in childIds) suffix += 1
        return "$candidate-$suffix"
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.parse(this)
} catch (_: IllegalArgumentException) {
    null
}
