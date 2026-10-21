package io.github.vietdsk6.porta.presentation.screens.packinspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.InvalidOpportunityPackException
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.OpportunityPackImportStatus
import io.github.vietdsk6.porta.domain.model.OpportunityPackVersionConflictException
import io.github.vietdsk6.porta.domain.model.PackDiagnostic
import io.github.vietdsk6.porta.domain.model.PackDiagnosticSeverity
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.util.extensions.asFormattedDate
import io.github.vietdsk6.porta.util.file.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PackInspectorViewModel(
    private val opportunityPackRepository: OpportunityPackRepository,
    private val bundledPackProvider: BundledOpportunityPackProvider,
    private val fileManager: FileManager,
) : ViewModel() {
    private val transientState = MutableStateFlow(PackInspectorTransientState())

    val uiState: StateFlow<PackInspectorUiState> = combine(
        opportunityPackRepository.observeActive(),
        transientState,
    ) { activePackResult, transient ->
        buildUiState(
            document = activePackResult.getOrNull(),
            loadError = activePackResult.exceptionOrNull(),
            transient = transient,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PackInspectorUiState(),
    )

    fun onUiEvent(event: PackInspectorUiEvent) {
        when (event) {
            is PackInspectorUiEvent.OpportunitySelected -> transientState.update {
                it.copy(selectedOpportunityId = event.opportunityId)
            }

            is PackInspectorUiEvent.ImportPack -> importPack(event.fileName, event.rawJson)

            PackInspectorUiEvent.ImportReadFailed -> showFailure(
                "Porta could not read that JSON file. The active pack was not changed.",
            )

            PackInspectorUiEvent.ExportActivePack -> exportActivePack()

            PackInspectorUiEvent.ExportDiagnostics -> exportDiagnostics()

            PackInspectorUiEvent.RestoreBundledRequested -> transientState.update {
                it.copy(isRestoreConfirmationVisible = true, message = null)
            }

            PackInspectorUiEvent.RestoreBundledCancelled -> transientState.update {
                it.copy(isRestoreConfirmationVisible = false)
            }

            PackInspectorUiEvent.RestoreBundledConfirmed -> restoreBundledPack()

            PackInspectorUiEvent.MessageHandled -> transientState.update { it.copy(message = null) }
        }
    }

    private fun importPack(fileName: String, rawJson: String) {
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    isWorking = true,
                    diagnostics = emptyList(),
                    message = null,
                )
            }
            opportunityPackRepository.importPack(rawJson).fold(
                onSuccess = { result ->
                    val message = when (result.status) {
                        OpportunityPackImportStatus.IMPORTED ->
                            "$fileName passed validation and is now the active pack."

                        OpportunityPackImportStatus.NO_CHANGE ->
                            "$fileName matches the active pack; nothing changed."
                    }
                    transientState.update {
                        it.copy(
                            isWorking = false,
                            selectedOpportunityId = result.document.opportunities.firstOrNull()?.id,
                            diagnostics = emptyList(),
                            message = message,
                        )
                    }
                },
                onFailure = ::handleImportFailure,
            )
        }
    }

    private fun handleImportFailure(error: Throwable) {
        val diagnostics = when (error) {
            is InvalidOpportunityPackException -> error.diagnostics.map { it.toUiState() }

            is OpportunityPackVersionConflictException -> listOf(
                PackDiagnosticUiState(
                    code = "version_conflict",
                    path = "version",
                    message = error.message.orEmpty(),
                    severity = PackDiagnosticSeverityUi.ERROR,
                ),
            )

            else -> emptyList()
        }
        transientState.update {
            it.copy(
                isWorking = false,
                diagnostics = diagnostics,
                message = "Import rejected. The active pack is unchanged.",
            )
        }
    }

    private fun exportActivePack() {
        viewModelScope.launch {
            transientState.update { it.copy(isWorking = true, message = null) }
            opportunityPackRepository.exportActive().fold(
                onSuccess = { rawJson ->
                    saveText(
                        suggestedName = "porta-opportunity-pack",
                        extension = "json",
                        content = rawJson,
                        successMessage = "Active opportunity pack exported.",
                    )
                },
                onFailure = { showFailure("Porta could not export the active pack.") },
            )
        }
    }

    private fun exportDiagnostics() {
        val diagnostics = transientState.value.diagnostics
        if (diagnostics.isEmpty()) return
        viewModelScope.launch {
            transientState.update { it.copy(isWorking = true, message = null) }
            val text = buildString {
                appendLine("Porta opportunity pack diagnostics")
                appendLine("Import was rejected; the active pack was not changed.")
                appendLine()
                diagnostics.forEach { diagnostic ->
                    appendLine("[${diagnostic.severity}] ${diagnostic.code}")
                    appendLine("Path: ${diagnostic.path}")
                    appendLine(diagnostic.message)
                    appendLine()
                }
            }
            saveText(
                suggestedName = "porta-pack-diagnostics",
                extension = "txt",
                content = text,
                successMessage = "Pack diagnostics exported.",
            )
        }
    }

    private fun restoreBundledPack() {
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    isWorking = true,
                    isRestoreConfirmationVisible = false,
                    diagnostics = emptyList(),
                    message = null,
                )
            }
            opportunityPackRepository.importPack(bundledPackProvider.rawJson()).fold(
                onSuccess = { result ->
                    transientState.update {
                        it.copy(
                            isWorking = false,
                            selectedOpportunityId = result.document.opportunities.firstOrNull()?.id,
                            message = "The bundled curated opportunity pack is active.",
                        )
                    }
                },
                onFailure = {
                    showFailure("Porta could not restore the bundled curated pack.")
                },
            )
        }
    }

    private suspend fun saveText(
        suggestedName: String,
        extension: String,
        content: String,
        successMessage: String,
    ) {
        fileManager.saveTextByPickingUpLocation(
            suggestedName = suggestedName,
            extension = extension,
            content = content,
        ).fold(
            onSuccess = { wasSaved ->
                transientState.update {
                    it.copy(
                        isWorking = false,
                        message = successMessage.takeIf { wasSaved },
                    )
                }
            },
            onFailure = { showFailure("Porta could not save that file.") },
        )
    }

    private fun showFailure(message: String) {
        transientState.update { it.copy(isWorking = false, message = message) }
    }

    private fun buildUiState(
        document: OpportunityPackDocument?,
        loadError: Throwable?,
        transient: PackInspectorTransientState,
    ): PackInspectorUiState {
        val activePack = document?.toUiState()
        val selectedId = transient.selectedOpportunityId
            ?.takeIf { candidate -> activePack?.opportunities?.any { it.id == candidate } == true }
            ?: activePack?.opportunities?.firstOrNull()?.id
        return PackInspectorUiState(
            isLoading = false,
            isWorking = transient.isWorking,
            activePack = activePack,
            selectedOpportunityId = selectedId,
            diagnostics = transient.diagnostics,
            isRestoreConfirmationVisible = transient.isRestoreConfirmationVisible,
            message = transient.message ?: loadError?.let {
                "Porta could not read the active pack. Restore the bundled curated pack or import a valid pack."
            },
        )
    }

    private fun OpportunityPackDocument.toUiState(): ActivePackUiState {
        val dates = opportunities.map(Opportunity::lastCheckedOn).sorted()
        val providers = opportunities.map(Opportunity::provider).distinct().sorted()
        return ActivePackUiState(
            id = metadata.id,
            name = metadata.name,
            publisher = metadata.publisher,
            versionLabel = "Version ${metadata.version}",
            schemaLabel = "Schema ${metadata.schemaVersion}",
            recordCountLabel = "${opportunities.size} records",
            checkedLabel = "Pack checked ${metadata.lastCheckedOn}",
            verificationRangeLabel = when {
                dates.isEmpty() -> "No opportunity verification dates"
                dates.first() == dates.last() -> "Records checked ${dates.first()}"
                else -> "Records checked ${dates.first()} – ${dates.last()}"
            },
            importedLabel = "Imported ${metadata.importedAtEpochMillis.asFormattedDate()}",
            sourceUrl = metadata.sourceUrl,
            isBundledPack = metadata.id == BundledOpportunityPackProvider.PACK_ID,
            providers = providers,
            opportunities = opportunities.map { it.toUiState() },
        )
    }

    private fun Opportunity.toUiState(): PackOpportunityUiState = PackOpportunityUiState(
        id = id,
        title = title,
        provider = provider,
        categoryLabel = category.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercaseChar),
        checkedLabel = "Checked $lastCheckedOn",
        sourceUrl = sourceUrl,
        sourceSection = sourceSection,
        ruleNodes = eligibilityRule.flatten(),
    )

    private fun RuleNode.flatten(depth: Int = 0): List<RuleNodeUiState> = when (this) {
        is RuleNode.All -> listOf(
            RuleNodeUiState(
                id = id,
                depth = depth,
                typeLabel = "ALL",
                summary = "Every one of ${children.size} child rules must match.",
            ),
        ) + children.flatMap { it.flatten(depth + 1) }

        is RuleNode.Any -> listOf(
            RuleNodeUiState(
                id = id,
                depth = depth,
                typeLabel = "ANY",
                summary = "At least one of ${children.size} child rules must match.",
            ),
        ) + children.flatMap { it.flatten(depth + 1) }

        is RuleNode.Atomic -> listOf(
            RuleNodeUiState(
                id = id,
                depth = depth,
                typeLabel = "ATOMIC · ${factKey.label()} ${operator.label()}" +
                    expected?.let { " ${it.displayValue()}" }.orEmpty(),
                summary = explanation,
                sourceNote = sourceNote,
            ),
        )

        is RuleNode.ManualCheck -> listOf(
            RuleNodeUiState(
                id = id,
                depth = depth,
                typeLabel = "MANUAL CHECK",
                summary = explanation,
                sourceNote = sourceNote,
            ),
        )
    }

    private fun FactKey.label(): String = name.replace('_', ' ')

    private fun Operator.label(): String = when (this) {
        Operator.EXISTS -> "exists"
        Operator.EQUALS -> "="
        Operator.NOT_EQUALS -> "≠"
        Operator.GREATER_THAN_OR_EQUAL -> "≥"
        Operator.LESS_THAN_OR_EQUAL -> "≤"
        Operator.IN_SET -> "in"
        Operator.CONTAINS_ANY -> "contains any"
        Operator.CONTAINS_ALL -> "contains all"
        Operator.DATE_BEFORE -> "before"
        Operator.DATE_AFTER -> "after"
    }

    private fun PackDiagnostic.toUiState(): PackDiagnosticUiState = PackDiagnosticUiState(
        code = code,
        path = path,
        message = message,
        severity = when (severity) {
            PackDiagnosticSeverity.ERROR -> PackDiagnosticSeverityUi.ERROR
            PackDiagnosticSeverity.WARNING -> PackDiagnosticSeverityUi.WARNING
        },
    )

    private data class PackInspectorTransientState(
        val isWorking: Boolean = false,
        val selectedOpportunityId: String? = null,
        val diagnostics: List<PackDiagnosticUiState> = emptyList(),
        val isRestoreConfirmationVisible: Boolean = false,
        val message: String? = null,
    )
}
