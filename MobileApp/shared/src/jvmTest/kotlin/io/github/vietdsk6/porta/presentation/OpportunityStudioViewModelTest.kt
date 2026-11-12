@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.OpportunityStudioDraftCompiler
import io.github.vietdsk6.porta.data.repository.RuleDraftRepository
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.data.source.preferences.FakeUserPreferences
import io.github.vietdsk6.porta.data.source.remote.draft.DraftService
import io.github.vietdsk6.porta.domain.model.DraftExpectedValue
import io.github.vietdsk6.porta.domain.model.DraftGroundingStatus
import io.github.vietdsk6.porta.domain.model.DraftReviewStatus
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.RawRuleDraftSuggestion
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioMetadataField
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioRuleTextField
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioUiEvent
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioViewModel
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.StudioNotice
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpportunityStudioViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: OpportunityPackRepository
    private lateinit var fileManager: RecordingFileManager
    private lateinit var viewModel: OpportunityStudioViewModel

    @BeforeTest
    fun setUp() = runTest(dispatcher) {
        Dispatchers.setMain(dispatcher)
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        val codec = OpportunityPackCodec()
        val parser = OpportunityPackParser(codec, OpportunityPackValidator())
        repository = OpportunityPackRepository(
            database.opportunityPackDao(),
            parser,
            BackgroundExecutor(dispatcher),
        )
        repository.importPack(BundledOpportunityPackProvider(codec).rawJson()).getOrThrow()
        fileManager = RecordingFileManager()
        viewModel = OpportunityStudioViewModel(
            opportunityPackRepository = repository,
            draftCompiler = OpportunityStudioDraftCompiler(codec, parser),
            fileManager = fileManager,
            currentLocalDateProvider = CurrentLocalDateProvider { LocalDate(2026, 11, 4) },
        )
        advanceUntilIdle()
    }

    @AfterTest
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `valid draft exports candidate without changing active pack`() = runTest(dispatcher) {
        val activeBefore = repository.exportActive().getOrThrow()
        assertFalse(viewModel.uiState.value.canExport)

        makeDraftValid()
        val candidate = assertNotNull(viewModel.uiState.value.rawJson)
        assertTrue(viewModel.uiState.value.canExport)
        assertTrue(candidate.contains("new-opportunity"))

        viewModel.onUiEvent(OpportunityStudioUiEvent.ExportDraft)
        advanceUntilIdle()

        assertEquals("porta-opportunity-pack-draft", fileManager.suggestedName)
        assertEquals("json", fileManager.extension)
        assertEquals(candidate, fileManager.content)
        assertEquals(activeBefore, repository.exportActive().getOrThrow())
        assertEquals(StudioNotice.DRAFT_EXPORTED, viewModel.uiState.value.message)
    }

    @Test
    fun `cancelled save does not report a false success`() = runTest(dispatcher) {
        fileManager.result = Result.success(false)
        makeDraftValid()

        viewModel.onUiEvent(OpportunityStudioUiEvent.ExportDraft)
        advanceUntilIdle()

        assertNotNull(fileManager.content)
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `invalid edits recompile live and block export`() = runTest(dispatcher) {
        makeDraftValid()
        assertTrue(viewModel.uiState.value.canExport)

        viewModel.onUiEvent(
            OpportunityStudioUiEvent.MetadataChanged(
                OpportunityStudioMetadataField.SOURCE_URL,
                "http://not-official.example",
            ),
        )

        assertFalse(viewModel.uiState.value.canExport)
        assertNull(viewModel.uiState.value.rawJson)
        assertTrue(viewModel.uiState.value.diagnostics.any { it.path.endsWith("sourceUrl") })
    }

    @Test
    fun `grounded draft remains inert until accepted and cannot be accepted twice`() = runTest(dispatcher) {
        installAiService(
            RawRuleDraftSuggestion(
                id = "age",
                kind = "ATOMIC",
                factKey = "AGE",
                operator = "GREATER_THAN_OR_EQUAL",
                expected = DraftExpectedValue("number", "18", "years"),
                actionability = "IMMUTABLE",
                quote = "at least 18 years old",
                rationale = "Entrants must be at least 18.",
            ),
        )
        advanceUntilIdle()
        val source = "Applicants must be at least 18 years old."
        val originalRuleCount = requireNotNull(viewModel.uiState.value.draft).rules.size
        viewModel.onUiEvent(OpportunityStudioUiEvent.PastedSourceChanged(source))
        viewModel.onUiEvent(OpportunityStudioUiEvent.DraftRulesRequested)
        advanceUntilIdle()

        val suggestion = viewModel.uiState.value.aiSuggestions.single()
        assertEquals(DraftGroundingStatus.GROUNDED, suggestion.groundingStatus)
        assertEquals(originalRuleCount, requireNotNull(viewModel.uiState.value.draft).rules.size)

        viewModel.onUiEvent(OpportunityStudioUiEvent.SuggestionAccepted(suggestion.id))
        val accepted = viewModel.uiState.value
        assertEquals(DraftReviewStatus.ACCEPTED, accepted.aiSuggestions.single().reviewStatus)
        assertEquals(originalRuleCount + 1, requireNotNull(accepted.draft).rules.size)
        val added = requireNotNull(accepted.draft).rules.last() as OpportunityStudioRuleDraft.Atomic
        assertTrue(added.sourceNote.contains(suggestion.quote))

        viewModel.onUiEvent(OpportunityStudioUiEvent.SuggestionAccepted(suggestion.id))
        assertEquals(originalRuleCount + 1, requireNotNull(viewModel.uiState.value.draft).rules.size)
    }

    @Test
    fun `ungrounded suggestion cannot enter draft and rejection leaves it untouched`() = runTest(dispatcher) {
        installAiService(
            RawRuleDraftSuggestion(
                id = "invented",
                kind = "ATOMIC",
                factKey = "AGE",
                operator = "GREATER_THAN_OR_EQUAL",
                expected = DraftExpectedValue("number", "18", "years"),
                quote = "Applicants are guaranteed an award",
                rationale = "This text is not present.",
            ),
        )
        advanceUntilIdle()
        val originalRuleCount = requireNotNull(viewModel.uiState.value.draft).rules.size
        viewModel.onUiEvent(OpportunityStudioUiEvent.PastedSourceChanged("Applicants must be at least 18."))
        viewModel.onUiEvent(OpportunityStudioUiEvent.DraftRulesRequested)
        advanceUntilIdle()

        val suggestion = viewModel.uiState.value.aiSuggestions.single()
        assertEquals(DraftGroundingStatus.REJECTED_UNGROUNDED, suggestion.groundingStatus)
        viewModel.onUiEvent(OpportunityStudioUiEvent.SuggestionAccepted(suggestion.id))
        assertEquals(originalRuleCount, requireNotNull(viewModel.uiState.value.draft).rules.size)
        viewModel.onUiEvent(OpportunityStudioUiEvent.SuggestionRejected(suggestion.id))
        assertEquals(DraftReviewStatus.REJECTED, viewModel.uiState.value.aiSuggestions.single().reviewStatus)
        assertEquals(originalRuleCount, requireNotNull(viewModel.uiState.value.draft).rules.size)
    }

    @Test
    fun `unsupported fact becomes manual check only after approval`() = runTest(dispatcher) {
        installAiService(
            RawRuleDraftSuggestion(
                id = "untyped",
                kind = "ATOMIC",
                factKey = "UNSUPPORTED_FACT",
                operator = "EQUALS",
                expected = DraftExpectedValue("boolean", "true"),
                quote = "A portfolio is required.",
                rationale = "Confirm the portfolio requirement.",
            ),
        )
        advanceUntilIdle()
        viewModel.onUiEvent(OpportunityStudioUiEvent.PastedSourceChanged("A portfolio is required."))
        viewModel.onUiEvent(OpportunityStudioUiEvent.DraftRulesRequested)
        advanceUntilIdle()

        val suggestion = viewModel.uiState.value.aiSuggestions.single()
        assertEquals(DraftGroundingStatus.GROUNDED, suggestion.groundingStatus)
        assertNotNull(suggestion.manualFallbackReason)
        viewModel.onUiEvent(OpportunityStudioUiEvent.SuggestionAccepted(suggestion.id))
        val added = requireNotNull(viewModel.uiState.value.draft).rules.last()
        assertTrue(added is OpportunityStudioRuleDraft.ManualCheck)
    }

    @Test
    fun `changing pasted source removes accepted AI rules but preserves manual rules`() = runTest(dispatcher) {
        installAiService(ageSuggestion())
        advanceUntilIdle()
        viewModel.onUiEvent(OpportunityStudioUiEvent.PastedSourceChanged("Applicants must be at least 18 years old."))
        viewModel.onUiEvent(OpportunityStudioUiEvent.DraftRulesRequested)
        advanceUntilIdle()
        viewModel.onUiEvent(OpportunityStudioUiEvent.SuggestionAccepted("ai-1"))
        viewModel.onUiEvent(OpportunityStudioUiEvent.AddManualRule)
        val before = requireNotNull(viewModel.uiState.value.draft)
        assertEquals(3, before.rules.size)

        viewModel.onUiEvent(OpportunityStudioUiEvent.PastedSourceChanged("A different official passage."))

        val after = requireNotNull(viewModel.uiState.value.draft)
        assertEquals(2, after.rules.size)
        assertTrue(after.rules.any { it.id == "manual-1" })
        assertTrue(after.rules.none { it.id.startsWith("ai-") })
        assertTrue(viewModel.uiState.value.aiSuggestions.isEmpty())
        assertTrue(viewModel.uiState.value.acceptedAiRuleIds.isEmpty())
    }

    @Test
    fun `accepted AI quote and ID stay locked while explanation stays editable`() = runTest(dispatcher) {
        installAiService(ageSuggestion())
        advanceUntilIdle()
        viewModel.onUiEvent(OpportunityStudioUiEvent.PastedSourceChanged("Applicants must be at least 18 years old."))
        viewModel.onUiEvent(OpportunityStudioUiEvent.DraftRulesRequested)
        advanceUntilIdle()
        viewModel.onUiEvent(OpportunityStudioUiEvent.SuggestionAccepted("ai-1"))
        val index = requireNotNull(viewModel.uiState.value.draft).rules.lastIndex
        val initial = requireNotNull(viewModel.uiState.value.draft).rules[index] as OpportunityStudioRuleDraft.Atomic

        viewModel.onUiEvent(
            OpportunityStudioUiEvent.RuleTextChanged(index, OpportunityStudioRuleTextField.SOURCE_NOTE, "Invented source"),
        )
        viewModel.onUiEvent(OpportunityStudioUiEvent.RuleTextChanged(index, OpportunityStudioRuleTextField.ID, "renamed"))
        val locked = requireNotNull(viewModel.uiState.value.draft).rules[index] as OpportunityStudioRuleDraft.Atomic
        assertEquals(initial.id, locked.id)
        assertEquals(initial.sourceNote, locked.sourceNote)

        viewModel.onUiEvent(
            OpportunityStudioUiEvent.RuleTextChanged(index, OpportunityStudioRuleTextField.EXPLANATION, "Human edited explanation."),
        )
        val edited = requireNotNull(viewModel.uiState.value.draft).rules[index] as OpportunityStudioRuleDraft.Atomic
        assertEquals("Human edited explanation.", edited.explanation)
    }

    private fun ageSuggestion() = RawRuleDraftSuggestion(
        id = "age",
        kind = "ATOMIC",
        factKey = "AGE",
        operator = "GREATER_THAN_OR_EQUAL",
        expected = DraftExpectedValue("number", "18", "years"),
        actionability = "IMMUTABLE",
        quote = "at least 18 years old",
        rationale = "Entrants must be at least 18.",
    )

    private fun installAiService(vararg suggestions: RawRuleDraftSuggestion) {
        val codec = OpportunityPackCodec()
        val parser = OpportunityPackParser(codec, OpportunityPackValidator())
        val service = object : DraftService {
            override suspend fun draft(sourceText: String) = suggestions.toList()
        }
        viewModel = OpportunityStudioViewModel(
            opportunityPackRepository = repository,
            draftCompiler = OpportunityStudioDraftCompiler(codec, parser),
            fileManager = fileManager,
            currentLocalDateProvider = CurrentLocalDateProvider { LocalDate(2026, 11, 4) },
            ruleDraftRepository = RuleDraftRepository(
                userPreferences = FakeUserPreferences(),
                demoDraftService = service,
                geminiDraftServiceFactory = { service },
                backgroundExecutor = BackgroundExecutor(dispatcher),
            ),
        )
    }

    private fun makeDraftValid() {
        listOf(
            OpportunityStudioMetadataField.TITLE to "Example scholarship",
            OpportunityStudioMetadataField.PROVIDER to "Example University",
            OpportunityStudioMetadataField.CYCLE to "2027",
            OpportunityStudioMetadataField.SOURCE_URL to "https://example.edu/scholarship",
        ).forEach { (field, value) ->
            viewModel.onUiEvent(OpportunityStudioUiEvent.MetadataChanged(field, value))
        }
        viewModel.onUiEvent(
            OpportunityStudioUiEvent.RuleTextChanged(
                ruleIndex = 0,
                field = OpportunityStudioRuleTextField.EXPLANATION,
                value = "Applicants must be enrolled.",
            ),
        )
        viewModel.onUiEvent(
            OpportunityStudioUiEvent.RuleTextChanged(
                ruleIndex = 0,
                field = OpportunityStudioRuleTextField.SOURCE_NOTE,
                value = "Eligibility · enrollment",
            ),
        )
    }

    private class RecordingFileManager : FileManager {
        var suggestedName: String? = null
        var extension: String? = null
        var content: String? = null
        var result: Result<Boolean> = Result.success(true)

        override suspend fun saveTextByPickingUpLocation(
            suggestedName: String,
            extension: String,
            content: String,
        ): Result<Boolean> {
            this.suggestedName = suggestedName
            this.extension = extension
            this.content = content
            return result
        }
    }
}
