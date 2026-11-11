package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.data.source.remote.draft.DraftService
import io.github.vietdsk6.porta.domain.model.RuleDraftSuggestion
import io.github.vietdsk6.porta.domain.model.toSafeSuggestion
import io.github.vietdsk6.porta.domain.usecase.QuoteGuard

/** Chooses the offline fixture or user-key service and grounds every untrusted suggestion. */
class RuleDraftRepository(
    private val userPreferences: UserPreferences,
    private val demoDraftService: DraftService,
    private val geminiDraftServiceFactory: (String) -> DraftService,
    private val backgroundExecutor: BackgroundExecutor,
) {
    suspend fun draft(sourceText: String): Result<List<RuleDraftSuggestion>> = backgroundExecutor.execute {
        require(sourceText.isNotBlank()) { "Paste source text before drafting." }
        val key = userPreferences.getString(UserPreferences.KEY_AI_DRAFT_API_KEY)?.trim().orEmpty()
        val service = if (key.isEmpty()) demoDraftService else geminiDraftServiceFactory(key)
        val suggestions = service.draft(sourceText)
        require(suggestions.size <= MAX_SUGGESTIONS) { "Too many draft suggestions were returned." }
        Result.success(suggestions.map { raw -> QuoteGuard.check(sourceText, raw.toSafeSuggestion()) })
    }

    private companion object {
        const val MAX_SUGGESTIONS = 20
    }
}
