package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.DraftGroundingStatus
import io.github.vietdsk6.porta.domain.model.DraftRuleKind
import io.github.vietdsk6.porta.domain.model.RuleDraftSuggestion
import io.github.vietdsk6.porta.domain.model.SourceTextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuoteGuardTest {
    private fun suggestion(quote: String) = RuleDraftSuggestion(
        id = "rule-1",
        kind = DraftRuleKind.MANUAL_CHECK,
        quote = quote,
        rationale = "Check this statement.",
    )

    @Test
    fun matchesTypographyAndWhitespaceWhileKeepingOriginalHighlightRange() {
        val source = "Before.  “full‑time”\n  applicants may apply. After."
        val checked = QuoteGuard.check(source, suggestion("\"full-time\" applicants may apply."))

        assertEquals(DraftGroundingStatus.GROUNDED, checked.groundingStatus)
        val start = source.indexOf('“')
        val end = source.indexOf("apply.") + "apply.".length
        assertEquals(SourceTextRange(start, end), checked.sourceRange)
        assertEquals("“full‑time”\n  applicants may apply.", source.substring(start, end))
        assertTrue(checked.canAccept)
    }

    @Test
    fun rejectsMissingEmptyAndPartialWordQuotes() {
        val source = "Students are eligible for this cycle."
        assertEquals(
            DraftGroundingStatus.REJECTED_UNGROUNDED,
            QuoteGuard.check(source, suggestion("Student are eligible")).groundingStatus,
        )
        assertEquals(
            DraftGroundingStatus.REJECTED_UNGROUNDED,
            QuoteGuard.check(source, suggestion("  \n  ")).groundingStatus,
        )
        val partial = QuoteGuard.check("ineligible applicants", suggestion("eligible"))
        assertNull(partial.sourceRange)
        assertFalse(partial.canAccept)
    }

    @Test
    fun doesNotTreatDifferentUnicodeCodePointsAsVerbatim() {
        val source = "Les étudiants sont éligibles."
        val decomposed = "e\u0301ligibles"
        val checked = QuoteGuard.check(source, suggestion(decomposed))

        assertEquals(DraftGroundingStatus.REJECTED_UNGROUNDED, checked.groundingStatus)
        assertNull(checked.sourceRange)
    }

    @Test
    fun allowsNonBreakingSpacesWithoutShiftingTheHighlight() {
        val source = "Eligible\u00a0students\u202fmay apply."
        val checked = QuoteGuard.check(source, suggestion("Eligible students may apply."))

        assertEquals(DraftGroundingStatus.GROUNDED, checked.groundingStatus)
        assertEquals(SourceTextRange(0, source.length), checked.sourceRange)
    }
}
