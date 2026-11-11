package io.github.vietdsk6.porta.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RuleDraftSuggestionTest {
    private fun raw(
        factKey: String? = "IELTS_SCORE",
        operator: String? = "GREATER_THAN_OR_EQUAL",
        expected: DraftExpectedValue? = DraftExpectedValue("number", "7.0", "band"),
    ) = RawRuleDraftSuggestion(
        id = "language",
        kind = "atomic",
        factKey = factKey,
        operator = operator,
        expected = expected,
        actionability = "CONTROLLABLE",
        quote = "IELTS score of 7.0",
        rationale = "Published language check.",
    )

    @Test
    fun acceptsOnlyTypedCatalogPredicate() {
        val suggestion = raw().toSafeSuggestion()

        assertEquals(DraftRuleKind.ATOMIC, suggestion.kind)
        assertEquals(FactKey.IELTS_SCORE, suggestion.factKey)
        assertEquals(Operator.GREATER_THAN_OR_EQUAL, suggestion.operator)
        assertEquals(FactValue.NumberValue(7.0, "band"), suggestion.expected)
        assertEquals(Actionability.CONTROLLABLE, suggestion.actionability)
    }

    @Test
    fun unsupportedFactOperatorAndUnitFallBackToManualCheck() {
        listOf(
            raw(factKey = "SCHOOL_RANK"),
            raw(operator = "PREDICTS_ADMISSION"),
            raw(expected = DraftExpectedValue("number", "7.0", "points")),
            raw(expected = DraftExpectedValue("number", "7.0", "months")),
        ).forEach { candidate ->
            val suggestion = candidate.toSafeSuggestion()
            assertEquals(DraftRuleKind.MANUAL_CHECK, suggestion.kind)
            assertNull(suggestion.factKey)
            assertNull(suggestion.operator)
            assertNull(suggestion.expected)
            kotlin.test.assertNotNull(suggestion.manualFallbackReason)
        }
    }

    @Test
    fun existsHasNoExpectedValueAndManualCandidateIsNeverSilentlyAtomic() {
        val exists = raw(operator = "EXISTS", expected = null).toSafeSuggestion()
        assertEquals(DraftRuleKind.ATOMIC, exists.kind)
        assertNull(exists.expected)

        val manual = RawRuleDraftSuggestion(
            id = "context",
            kind = "manual_check",
            quote = "Confirm the official conditions.",
            rationale = "Cannot encode this safely.",
        ).toSafeSuggestion()
        assertEquals(DraftRuleKind.MANUAL_CHECK, manual.kind)
        assertNull(manual.factKey)
    }
}
