package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.preferences.FakeUserPreferences
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.data.source.remote.draft.DraftService
import io.github.vietdsk6.porta.domain.model.DraftGroundingStatus
import io.github.vietdsk6.porta.domain.model.DraftRuleKind
import io.github.vietdsk6.porta.domain.model.RawRuleDraftSuggestion
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RuleDraftRepositoryTest {
    @Test
    fun offlineSuggestionsAreGroundedAndUnknownFactsBecomeManualChecks() = runTest {
        val preferences = FakeUserPreferences()
        var remoteCalled = false
        val repository = RuleDraftRepository(
            userPreferences = preferences,
            demoDraftService = service(
                RawRuleDraftSuggestion(
                    id = "draft-1",
                    kind = "ATOMIC",
                    factKey = "INVENTED_FACT",
                    operator = "EQUALS",
                    quote = "at least 18",
                    rationale = "Check the threshold.",
                ),
            ),
            geminiDraftServiceFactory = {
                remoteCalled = true
                service()
            },
            backgroundExecutor = BackgroundExecutor(EmptyCoroutineContext),
        )

        val suggestion = repository.draft("Applicants must be at least 18 years old.").getOrThrow().single()

        assertFalse(remoteCalled)
        assertEquals(DraftRuleKind.MANUAL_CHECK, suggestion.kind)
        assertEquals(DraftGroundingStatus.GROUNDED, suggestion.groundingStatus)
        assertEquals("at least 18", suggestion.sourceRange?.let { "Applicants must be at least 18 years old.".substring(it.start, it.endExclusive) })
    }

    @Test
    fun configuredKeyChoosesRemoteAndRejectsUnmatchedQuote() = runTest {
        val preferences = FakeUserPreferences()
        preferences.putString(UserPreferences.KEY_AI_DRAFT_API_KEY, "personal-test-key")
        var receivedKey: String? = null
        val repository = RuleDraftRepository(
            userPreferences = preferences,
            demoDraftService = service(),
            geminiDraftServiceFactory = { key ->
                receivedKey = key
                service(
                    RawRuleDraftSuggestion(
                        id = "draft-1",
                        kind = "MANUAL_CHECK",
                        quote = "a condition absent from the source",
                        rationale = "Review this.",
                    ),
                )
            },
            backgroundExecutor = BackgroundExecutor(EmptyCoroutineContext),
        )

        val suggestion = repository.draft("Only source text is trusted.").getOrThrow().single()

        assertEquals("personal-test-key", receivedKey)
        assertEquals(DraftGroundingStatus.REJECTED_UNGROUNDED, suggestion.groundingStatus)
        assertFalse(suggestion.canAccept)
    }

    private fun service(vararg suggestions: RawRuleDraftSuggestion): DraftService = object : DraftService {
        override suspend fun draft(sourceText: String) = suggestions.toList()
    }
}
