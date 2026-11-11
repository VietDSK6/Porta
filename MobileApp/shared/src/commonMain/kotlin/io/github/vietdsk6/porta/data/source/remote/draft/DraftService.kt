package io.github.vietdsk6.porta.data.source.remote.draft

import io.github.vietdsk6.porta.domain.model.RawRuleDraftSuggestion

/** Returns untrusted candidates. Callers must apply QuoteGuard and require human review. */
interface DraftService {
    suspend fun draft(sourceText: String): List<RawRuleDraftSuggestion>
}

class DraftServiceException(message: String) : IllegalStateException(message)
