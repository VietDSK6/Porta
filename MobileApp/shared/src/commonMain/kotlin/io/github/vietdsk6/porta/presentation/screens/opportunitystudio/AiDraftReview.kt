package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import io.github.vietdsk6.porta.domain.model.DraftRuleKind
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.OpportunityStudioExpectedValueDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.RuleDraftSuggestion
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind

/** A human-approved suggestion enters the same editable rule builder as a hand-written rule. */
internal fun RuleDraftSuggestion.asStudioRule(ruleId: String, sourceUrl: String): OpportunityStudioRuleDraft {
    val note = "Verbatim excerpt from pasted source: “${quote.trim()}”. " +
        "Curator must verify against $sourceUrl before publishing."
    val explanation = listOfNotNull(rationale.trim(), manualFallbackReason?.trim())
        .filter(String::isNotEmpty)
        .joinToString(" ")
    val key = factKey
    val comparison = operator
    return if (kind == DraftRuleKind.ATOMIC && key != null && comparison != null) {
        OpportunityStudioRuleDraft.Atomic(
            id = ruleId,
            factKey = key,
            operator = comparison,
            expected = expected?.asStudioExpected(),
            explanation = explanation,
            sourceNote = note,
            actionability = actionability,
        )
    } else {
        OpportunityStudioRuleDraft.ManualCheck(
            id = ruleId,
            explanation = explanation,
            sourceNote = note,
        )
    }
}

private fun FactValue.asStudioExpected(): OpportunityStudioExpectedValueDraft = when (this) {
    is FactValue.BooleanValue -> OpportunityStudioExpectedValueDraft(StudioFactValueKind.BOOLEAN, value.toString())

    is FactValue.NumberValue -> OpportunityStudioExpectedValueDraft(StudioFactValueKind.NUMBER, amount.toString(), unit)

    is FactValue.EnumValue -> OpportunityStudioExpectedValueDraft(StudioFactValueKind.ENUM, value)

    is FactValue.EnumListValue -> OpportunityStudioExpectedValueDraft(
        StudioFactValueKind.ENUM_LIST,
        values.sorted().joinToString(", "),
    )

    is FactValue.DateValue -> OpportunityStudioExpectedValueDraft(StudioFactValueKind.DATE, value.toString())

    is FactValue.DurationValue -> OpportunityStudioExpectedValueDraft(StudioFactValueKind.DURATION, amount.toString(), unit.name)

    is FactValue.TextValue -> OpportunityStudioExpectedValueDraft(StudioFactValueKind.TEXT, value)
}
