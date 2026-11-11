package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.DraftGroundingStatus
import io.github.vietdsk6.porta.domain.model.RuleDraftSuggestion
import io.github.vietdsk6.porta.domain.model.SourceTextRange

/** Finds a quote in the pasted text after only typography and whitespace normalization. */
object QuoteGuard {
    fun check(sourceText: String, suggestion: RuleDraftSuggestion): RuleDraftSuggestion {
        val source = normalize(sourceText)
        val quote = normalize(suggestion.quote).text.trim()
        if (quote.isEmpty()) return suggestion.rejected()

        var offset = source.text.indexOf(quote)
        while (offset >= 0) {
            val end = offset + quote.length
            if (source.hasWordBoundaries(offset, end, quote)) {
                return suggestion.copy(
                    groundingStatus = DraftGroundingStatus.GROUNDED,
                    sourceRange = SourceTextRange(source.starts[offset], source.ends[end - 1]),
                )
            }
            offset = source.text.indexOf(quote, startIndex = offset + 1)
        }
        return suggestion.rejected()
    }

    private fun RuleDraftSuggestion.rejected() = copy(
        groundingStatus = DraftGroundingStatus.REJECTED_UNGROUNDED,
        sourceRange = null,
    )

    private data class NormalizedText(
        val text: String,
        val starts: List<Int>,
        val ends: List<Int>,
    ) {
        fun hasWordBoundaries(start: Int, end: Int, quote: String): Boolean {
            val leftIsInsideWord = quote.first().isLetterOrDigit() && start > 0 && text[start - 1].isLetterOrDigit()
            val rightIsInsideWord = quote.last().isLetterOrDigit() && end < text.length && text[end].isLetterOrDigit()
            return !leftIsInsideWord && !rightIsInsideWord
        }
    }

    private fun normalize(input: String): NormalizedText {
        val text = StringBuilder(input.length)
        val starts = mutableListOf<Int>()
        val ends = mutableListOf<Int>()
        input.forEachIndexed { index, original ->
            val normalized = when (original) {
                '‘', '’', '‚', '‛', '‹', '›' -> '\''
                '“', '”', '„', '‟', '«', '»' -> '"'
                '‐', '‑', '‒', '–', '—', '―', '−' -> '-'
                else -> original
            }
            if (normalized.isWhitespace() || normalized == '\u00a0' || normalized == '\u202f') {
                if (text.isNotEmpty() && text.last() != ' ') {
                    text.append(' ')
                    starts += index
                    ends += index + 1
                } else if (text.isNotEmpty() && text.last() == ' ') {
                    ends[ends.lastIndex] = index + 1
                }
            } else {
                text.append(normalized)
                starts += index
                ends += index + 1
            }
        }
        if (text.isNotEmpty() && text.last() == ' ') {
            text.setLength(text.length - 1)
            starts.removeAt(starts.lastIndex)
            ends.removeAt(ends.lastIndex)
        }
        return NormalizedText(text.toString(), starts, ends)
    }
}
