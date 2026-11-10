package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityStudioDraftCompiler
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.OpportunityStudioDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioExpectedValueDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.StudioDeadlineKind
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.domain.model.StudioRootRuleKind
import io.github.vietdsk6.porta.util.Platform
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.getPlatform
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
) : ViewModel() {
    private var baseDocument: OpportunityPackDocument? = null
    private val _uiState = MutableStateFlow(
        OpportunityStudioUiState(isSupported = getPlatform() == Platform.Desktop),
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

    private fun loadActivePack() = viewModelScope.launch {
        opportunityPackRepository.getActive().fold(
            onSuccess = { document ->
                if (document == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "No active opportunity pack is available. Restore or import a pack first.",
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
                        message = "Porta could not load the active pack for authoring.",
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
        recompile(
            draft.copy(rules = draft.rules.filterIndexed { index, _ -> index != ruleIndex }),
            selectedRuleIndex = (ruleIndex - 1).takeIf { it >= 0 },
        )
    }

    private fun updateRuleText(event: OpportunityStudioUiEvent.RuleTextChanged) {
        val draft = _uiState.value.draft ?: return
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
        if (!state.isSupported || getPlatform() != Platform.Desktop) {
            _uiState.update { it.copy(message = "Opportunity Studio export is available on Desktop.") }
            return
        }
        val rawJson = state.rawJson
        if (!state.canExport || rawJson == null) {
            _uiState.update { it.copy(message = "Resolve every validation issue before exporting.") }
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
                                "Draft exported. Review and activate it from Pack inspector."
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
                            message = "Porta could not save the draft file.",
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
