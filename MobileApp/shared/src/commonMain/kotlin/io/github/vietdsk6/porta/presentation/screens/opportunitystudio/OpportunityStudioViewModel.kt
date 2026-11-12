package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.OpportunityStudioDraftCompiler
import io.github.vietdsk6.porta.data.repository.RuleDraftRepository
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.DraftReviewStatus
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.OpportunityStudioDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioExpectedValueDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.RuleDraftSuggestion
import io.github.vietdsk6.porta.domain.model.StudioDeadlineKind
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.domain.model.StudioRootRuleKind
import io.github.vietdsk6.porta.domain.usecase.QuoteGuard
import io.github.vietdsk6.porta.util.PlatformCapabilities
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.getPlatformCapabilities
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OpportunityStudioViewModel(
    private val opportunityPackRepository: OpportunityPackRepository,
    private val draftCompiler: OpportunityStudioDraftCompiler,
    private val fileManager: FileManager,
    private val currentLocalDateProvider: CurrentLocalDateProvider,
    private val platformCapabilities: PlatformCapabilities = getPlatformCapabilities(),
    private val ruleDraftRepository: RuleDraftRepository? = null,
) : ViewModel() {
    private var baseDocument: OpportunityPackDocument? = null
    private val _uiState = MutableStateFlow(
        OpportunityStudioUiState(isSupported = platformCapabilities.supportsStudio),
    )
    val uiState: StateFlow<OpportunityStudioUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value.isSupported) {
            loadActivePack()
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onUiEvent(event: OpportunityStudioUiEvent) {
        when (event) {
            is OpportunityStudioUiEvent.PastedSourceChanged -> changeAiSource(event.value)

            is OpportunityStudioUiEvent.DemoSourceSelected -> changeAiSource(event.sourceText, event.sourceUrl)

            OpportunityStudioUiEvent.DraftRulesRequested -> requestAiDraft()

            is OpportunityStudioUiEvent.SuggestionEditRequested -> _uiState.update { state ->
                val suggestion = state.aiSuggestions.firstOrNull { it.id == event.suggestionId }
                if (suggestion?.reviewStatus != DraftReviewStatus.PENDING) {
                    state
                } else {
                    state.copy(
                        editingSuggestionId = event.suggestionId.takeUnless { it == state.editingSuggestionId },
                    )
                }
            }

            is OpportunityStudioUiEvent.SuggestionQuoteChanged -> updateSuggestion(event.suggestionId) { suggestion ->
                QuoteGuard.check(_uiState.value.pastedSourceText, suggestion.copy(quote = event.quote))
            }

            is OpportunityStudioUiEvent.SuggestionRationaleChanged -> updateSuggestion(event.suggestionId) {
                it.copy(rationale = event.rationale)
            }

            is OpportunityStudioUiEvent.SuggestionAccepted -> acceptSuggestion(event.suggestionId)

            is OpportunityStudioUiEvent.SuggestionRejected -> updateSuggestion(event.suggestionId) {
                it.copy(reviewStatus = DraftReviewStatus.REJECTED)
            }

            is OpportunityStudioUiEvent.MetadataChanged -> updateDraft { draft ->
                when (event.field) {
                    OpportunityStudioMetadataField.OPPORTUNITY_ID -> draft.copy(opportunityId = event.value)
                    OpportunityStudioMetadataField.TITLE -> draft.copy(title = event.value)
                    OpportunityStudioMetadataField.PROVIDER -> draft.copy(provider = event.value)
                    OpportunityStudioMetadataField.CYCLE -> draft.copy(cycle = event.value)
                    OpportunityStudioMetadataField.CYCLE_START_ON -> draft.copy(cycleStartOn = event.value)
                    OpportunityStudioMetadataField.SOURCE_URL -> draft.copy(sourceUrl = event.value)
                    OpportunityStudioMetadataField.SOURCE_SECTION -> draft.copy(sourceSection = event.value)
                    OpportunityStudioMetadataField.LAST_CHECKED_ON -> draft.copy(lastCheckedOn = event.value)
                    OpportunityStudioMetadataField.DEADLINE_DATE -> draft.copy(deadlineDate = event.value)
                }
            }

            is OpportunityStudioUiEvent.CategoryChanged -> updateDraft { it.copy(category = event.category) }

            is OpportunityStudioUiEvent.DeadlineKindChanged -> updateDraft { draft ->
                draft.copy(deadlineKind = event.kind)
            }

            is OpportunityStudioUiEvent.RootRuleKindChanged -> updateDraft { draft ->
                draft.copy(rootRuleKind = event.kind)
            }

            OpportunityStudioUiEvent.AddAtomicRule -> addAtomicRule()

            OpportunityStudioUiEvent.AddManualRule -> addManualRule()

            is OpportunityStudioUiEvent.RuleSelected -> _uiState.update { it.copy(selectedRuleIndex = event.ruleIndex) }

            is OpportunityStudioUiEvent.RuleRemoved -> removeRule(event.ruleIndex)

            is OpportunityStudioUiEvent.RuleTextChanged -> updateRuleText(event)

            is OpportunityStudioUiEvent.AtomicFactKeyChanged -> updateAtomic(event.ruleIndex) {
                it.copy(factKey = event.factKey)
            }

            is OpportunityStudioUiEvent.AtomicOperatorChanged -> updateAtomic(event.ruleIndex) { rule ->
                rule.copy(
                    operator = event.operator,
                    expected = when {
                        event.operator == Operator.EXISTS -> null
                        rule.expected != null -> rule.expected
                        else -> defaultExpectedValue()
                    },
                )
            }

            is OpportunityStudioUiEvent.AtomicActionabilityChanged -> updateAtomic(event.ruleIndex) {
                it.copy(actionability = event.actionability)
            }

            is OpportunityStudioUiEvent.AtomicExpectedKindChanged -> updateAtomic(event.ruleIndex) {
                it.copy(expected = defaultExpectedValue(event.kind))
            }

            OpportunityStudioUiEvent.ToggleJsonPreview -> _uiState.update {
                it.copy(isJsonPreviewVisible = !it.isJsonPreviewVisible)
            }

            OpportunityStudioUiEvent.ExportDraft -> exportDraft()

            OpportunityStudioUiEvent.MessageHandled -> _uiState.update { it.copy(message = null) }
        }
    }

    private fun changeAiSource(sourceText: String, sourceUrl: String? = null) {
        val acceptedRuleIds = _uiState.value.acceptedAiRuleIds
        _uiState.update { state ->
            state.copy(
                pastedSourceText = sourceText,
                isDraftingRules = false,
                aiSuggestions = emptyList(),
                acceptedAiRuleIds = emptySet(),
                editingSuggestionId = null,
                aiDraftMessage = if (acceptedRuleIds.isNotEmpty()) {
                    StudioNotice.SOURCE_CHANGED
                } else {
                    null
                },
            )
        }
        if (acceptedRuleIds.isNotEmpty() || sourceUrl != null) {
            updateDraft { draft ->
                draft.copy(
                    sourceUrl = sourceUrl ?: draft.sourceUrl,
                    rules = draft.rules.filterNot { it.id in acceptedRuleIds },
                )
            }
        }
    }

    private fun requestAiDraft() {
        val state = _uiState.value
        val source = state.pastedSourceText
        val repository = ruleDraftRepository
        if (!state.isSupported || state.draft == null || source.isBlank() || state.isDraftingRules) return
        if (repository == null) {
            _uiState.update { it.copy(aiDraftMessage = StudioNotice.DRAFT_UNAVAILABLE) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDraftingRules = true, aiDraftMessage = null) }
            repository.draft(source).fold(
                onSuccess = { suggestions ->
                    _uiState.update { current ->
                        if (current.pastedSourceText != source) {
                            current
                        } else {
                            current.copy(
                                isDraftingRules = false,
                                aiSuggestions = suggestions.mapIndexed { index, suggestion ->
                                    suggestion.copy(id = "ai-${index + 1}")
                                },
                                editingSuggestionId = null,
                                aiDraftMessage = if (suggestions.isEmpty()) {
                                    StudioNotice.NO_RULES_SUGGESTED
                                } else {
                                    null
                                },
                            )
                        }
                    }
                },
                onFailure = {
                    _uiState.update { current ->
                        if (current.pastedSourceText != source) {
                            current
                        } else {
                            current.copy(
                                isDraftingRules = false,
                                aiDraftMessage = StudioNotice.DRAFT_FAILED,
                            )
                        }
                    }
                },
            )
        }
    }

    private fun updateSuggestion(id: String, transform: (RuleDraftSuggestion) -> RuleDraftSuggestion) {
        _uiState.update { state ->
            val updated = state.aiSuggestions.map { suggestion ->
                if (suggestion.id == id && suggestion.reviewStatus == DraftReviewStatus.PENDING) {
                    transform(suggestion)
                } else {
                    suggestion
                }
            }
            state.copy(
                aiSuggestions = updated,
                editingSuggestionId = state.editingSuggestionId.takeUnless { editingId ->
                    updated.firstOrNull { it.id == editingId }?.reviewStatus != DraftReviewStatus.PENDING
                },
            )
        }
    }

    private fun acceptSuggestion(id: String) {
        val state = _uiState.value
        val suggestion = state.aiSuggestions.firstOrNull { it.id == id } ?: return
        if (!suggestion.canAccept) {
            _uiState.update { it.copy(aiDraftMessage = StudioNotice.QUOTE_REQUIRED) }
            return
        }
        val draft = state.draft ?: return
        val index = draft.rules.size
        val ruleId = draft.nextRuleId("ai")
        val rule = suggestion.asStudioRule(ruleId, draft.sourceUrl)
        recompile(draft.copy(rules = draft.rules + rule), selectedRuleIndex = index)
        _uiState.update { current ->
            current.copy(
                aiSuggestions = current.aiSuggestions.map { item ->
                    if (item.id == id) item.copy(reviewStatus = DraftReviewStatus.ACCEPTED) else item
                },
                acceptedAiRuleIds = current.acceptedAiRuleIds + ruleId,
                editingSuggestionId = null,
                aiDraftMessage = StudioNotice.RULE_ADDED,
            )
        }
    }

    private fun loadActivePack() = viewModelScope.launch {
        opportunityPackRepository.getActive().fold(
            onSuccess = { document ->
                if (document == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = StudioNotice.PACK_MISSING,
                        )
                    }
                    return@fold
                }
                baseDocument = document
                val draft = newDraft(currentLocalDateProvider.today().toString())
                val compilation = draftCompiler.compile(document, draft)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        basePackName = document.metadata.name,
                        basePackVersion = document.metadata.version,
                        nextPackVersion = compilation.nextPackVersion,
                        draft = draft,
                        selectedRuleIndex = 0,
                        diagnostics = compilation.diagnostics.map { diagnostic ->
                            OpportunityStudioDiagnosticUiState(diagnostic.path, diagnostic.message)
                        },
                        rawJson = compilation.rawJson,
                    )
                }
            },
            onFailure = {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = StudioNotice.PACK_LOAD_FAILED,
                    )
                }
            },
        )
    }

    private fun updateDraft(transform: (OpportunityStudioDraft) -> OpportunityStudioDraft) {
        val current = _uiState.value.draft ?: return
        recompile(transform(current))
    }

    private fun recompile(draft: OpportunityStudioDraft, selectedRuleIndex: Int? = _uiState.value.selectedRuleIndex) {
        val document = baseDocument ?: return
        val compilation = draftCompiler.compile(document, draft)
        _uiState.update {
            it.copy(
                draft = draft,
                selectedRuleIndex = selectedRuleIndex?.takeIf { it in draft.rules.indices }
                    ?: draft.rules.indices.firstOrNull(),
                nextPackVersion = compilation.nextPackVersion,
                diagnostics = compilation.diagnostics.map { diagnostic ->
                    OpportunityStudioDiagnosticUiState(diagnostic.path, diagnostic.message)
                },
                rawJson = compilation.rawJson,
                message = null,
            )
        }
    }

    private fun addAtomicRule() {
        val draft = _uiState.value.draft ?: return
        val id = draft.nextRuleId("atomic")
        val rule = OpportunityStudioRuleDraft.Atomic(
            id = id,
            factKey = FactKey.ENROLLED,
            operator = Operator.EQUALS,
            expected = defaultExpectedValue(),
            explanation = "",
            sourceNote = "",
            actionability = Actionability.CONTEXT_ONLY,
        )
        recompile(draft.copy(rules = draft.rules + rule), selectedRuleIndex = draft.rules.size)
    }

    private fun addManualRule() {
        val draft = _uiState.value.draft ?: return
        val id = draft.nextRuleId("manual")
        val rule = OpportunityStudioRuleDraft.ManualCheck(
            id = id,
            explanation = "",
        )
        recompile(draft.copy(rules = draft.rules + rule), selectedRuleIndex = draft.rules.size)
    }

    private fun removeRule(ruleIndex: Int) {
        val draft = _uiState.value.draft ?: return
        if (ruleIndex !in draft.rules.indices) return
        val removedId = draft.rules[ruleIndex].id
        recompile(
            draft.copy(rules = draft.rules.filterIndexed { index, _ -> index != ruleIndex }),
            selectedRuleIndex = (ruleIndex - 1).takeIf { it >= 0 },
        )
        _uiState.update { it.copy(acceptedAiRuleIds = it.acceptedAiRuleIds - removedId) }
    }

    private fun updateRuleText(event: OpportunityStudioUiEvent.RuleTextChanged) {
        val state = _uiState.value
        val draft = state.draft ?: return
        if (draft.rules.getOrNull(event.ruleIndex)?.id in state.acceptedAiRuleIds &&
            event.field in setOf(OpportunityStudioRuleTextField.ID, OpportunityStudioRuleTextField.SOURCE_NOTE)
        ) {
            _uiState.update {
                it.copy(aiDraftMessage = StudioNotice.AI_RULE_LOCKED)
            }
            return
        }
        val rules = draft.rules.mapIndexed { index, rule ->
            if (index != event.ruleIndex) return@mapIndexed rule
            when (rule) {
                is OpportunityStudioRuleDraft.Atomic -> when (event.field) {
                    OpportunityStudioRuleTextField.ID -> rule.copy(id = event.value)

                    OpportunityStudioRuleTextField.EXPECTED_VALUE -> rule.copy(
                        expected = rule.expected?.copy(value = event.value),
                    )

                    OpportunityStudioRuleTextField.EXPECTED_UNIT -> rule.copy(
                        expected = rule.expected?.copy(unit = event.value),
                    )

                    OpportunityStudioRuleTextField.EXPLANATION -> rule.copy(explanation = event.value)

                    OpportunityStudioRuleTextField.SOURCE_NOTE -> rule.copy(sourceNote = event.value)
                }

                is OpportunityStudioRuleDraft.ManualCheck -> when (event.field) {
                    OpportunityStudioRuleTextField.ID -> rule.copy(id = event.value)

                    OpportunityStudioRuleTextField.EXPLANATION -> rule.copy(explanation = event.value)

                    OpportunityStudioRuleTextField.SOURCE_NOTE -> rule.copy(sourceNote = event.value)

                    OpportunityStudioRuleTextField.EXPECTED_VALUE,
                    OpportunityStudioRuleTextField.EXPECTED_UNIT,
                    -> rule
                }
            }
        }
        recompile(draft = draft.copy(rules = rules), selectedRuleIndex = event.ruleIndex)
    }

    private fun updateAtomic(
        ruleIndex: Int,
        transform: (OpportunityStudioRuleDraft.Atomic) -> OpportunityStudioRuleDraft.Atomic,
    ) {
        updateDraft { draft ->
            draft.copy(
                rules = draft.rules.mapIndexed { index, rule ->
                    if (index == ruleIndex && rule is OpportunityStudioRuleDraft.Atomic) transform(rule) else rule
                },
            )
        }
    }

    private fun exportDraft() {
        val state = _uiState.value
        if (!state.isSupported || !platformCapabilities.supportsStudio) {
            _uiState.update { it.copy(message = StudioNotice.EXPORT_UNAVAILABLE) }
            return
        }
        val rawJson = state.rawJson
        if (!state.canExport || rawJson == null) {
            _uiState.update { it.copy(message = StudioNotice.VALIDATION_REQUIRED) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, message = null) }
            fileManager.saveTextByPickingUpLocation(
                suggestedName = "porta-opportunity-pack-draft",
                extension = "json",
                content = rawJson,
            ).fold(
                onSuccess = { wasSaved ->
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            message = if (wasSaved) {
                                StudioNotice.DRAFT_EXPORTED
                            } else {
                                null
                            },
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isWorking = false,
                            message = StudioNotice.SAVE_FAILED,
                        )
                    }
                },
            )
        }
    }

    private fun newDraft(today: String): OpportunityStudioDraft = OpportunityStudioDraft(
        opportunityId = "new-opportunity",
        title = "",
        provider = "",
        category = OpportunityCategory.SCHOLARSHIP,
        cycle = "",
        sourceUrl = "https://",
        lastCheckedOn = today,
        deadlineKind = StudioDeadlineKind.TBA,
        rootRuleKind = StudioRootRuleKind.ALL,
        rules = listOf(
            OpportunityStudioRuleDraft.Atomic(
                id = "atomic-1",
                factKey = FactKey.ENROLLED,
                operator = Operator.EQUALS,
                expected = defaultExpectedValue(),
                explanation = "",
                sourceNote = "",
                actionability = Actionability.CONTEXT_ONLY,
            ),
        ),
    )

    private fun defaultExpectedValue(
        kind: StudioFactValueKind = StudioFactValueKind.BOOLEAN,
    ): OpportunityStudioExpectedValueDraft = OpportunityStudioExpectedValueDraft(
        kind = kind,
        value = when (kind) {
            StudioFactValueKind.BOOLEAN -> "true"

            StudioFactValueKind.NUMBER -> "0"

            StudioFactValueKind.ENUM,
            StudioFactValueKind.ENUM_LIST,
            StudioFactValueKind.TEXT,
            StudioFactValueKind.DATE,
            -> ""

            StudioFactValueKind.DURATION -> "0"
        },
        unit = when (kind) {
            StudioFactValueKind.NUMBER -> "unit"
            StudioFactValueKind.DURATION -> "DAYS"
            else -> ""
        },
    )

    private fun OpportunityStudioDraft.nextRuleId(prefix: String): String {
        val usedIds = rules.mapTo(mutableSetOf(), OpportunityStudioRuleDraft::id)
        var index = 1
        while ("$prefix-$index" in usedIds) index += 1
        return "$prefix-$index"
    }
}
