package io.github.vietdsk6.porta.presentation.screens.packinspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.scenario.BundledPackSource
import io.github.vietdsk6.porta.domain.model.DEFAULT_SOURCE_REVIEW_WINDOW_DAYS
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.InvalidOpportunityPackException
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.OpportunityPackImportPreview
import io.github.vietdsk6.porta.domain.model.OpportunityPackImportStatus
import io.github.vietdsk6.porta.domain.model.OpportunityPackVersionConflictException
import io.github.vietdsk6.porta.domain.model.PackDiagnostic
import io.github.vietdsk6.porta.domain.model.PackDiagnosticSeverity
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.sourceFreshness
import io.github.vietdsk6.porta.domain.model.staleRuleAssertionsFor
import io.github.vietdsk6.porta.presentation.components.sourceReviewLabel
import io.github.vietdsk6.porta.presentation.components.sourceReviewNeedsAttention
import io.github.vietdsk6.porta.presentation.locale.asLocalizedDate
import io.github.vietdsk6.porta.presentation.locale.displayValue
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class PackInspectorViewModel(
    private val opportunityPackRepository: OpportunityPackRepository,
    private val profileRepository: ProfileRepository,
    private val bundledPackSource: BundledPackSource,
    private val fileManager: FileManager,
    private val currentLocalDateProvider: CurrentLocalDateProvider,
) : ViewModel() {
    private val transientState = MutableStateFlow(PackInspectorTransientState())

    val uiState: StateFlow<PackInspectorUiState> = combine(
        opportunityPackRepository.observeActive(),
        transientState,
        currentLocalDateProvider.currentDate,
    ) { activePackResult, transient, today ->
        buildUiState(
            document = activePackResult.getOrNull(),
            loadError = activePackResult.exceptionOrNull(),
            transient = transient,
            today = today,
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

            PackInspectorUiEvent.ImportPreviewCancelled -> transientState.update {
                it.copy(pendingImport = null)
            }

            PackInspectorUiEvent.ImportPreviewConfirmed -> confirmPendingImport()

            PackInspectorUiEvent.ImportReadFailed -> showFailure(
                PackInspectorNotice.ImportReadFailed,
            )

            PackInspectorUiEvent.ExportActivePack -> exportActivePack()

            PackInspectorUiEvent.ExportDiagnostics -> exportDiagnostics()

            PackInspectorUiEvent.RestoreBundledRequested -> importPack(
                fileName = "Porta bundled curated pack",
                rawJson = bundledPackSource.rawJson(),
            )

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
            opportunityPackRepository.previewImport(rawJson).fold(
                onSuccess = { preview ->
                    val matchesActiveVersion = preview.diff.previousPackId == preview.diff.candidatePackId &&
                        preview.diff.previousVersion == preview.diff.candidateVersion &&
                        !preview.diff.hasChanges
                    if (matchesActiveVersion) {
                        transientState.update {
                            it.copy(
                                isWorking = false,
                                pendingImport = null,
                                message = PackInspectorNotice.MatchesActive(fileName),
                            )
                        }
                        return@fold
                    }
                    val profile = profileRepository.getProfile().getOrNull()
                    transientState.update {
                        it.copy(
                            isWorking = false,
                            diagnostics = emptyList(),
                            pendingImport = PendingPackImport(
                                rawJson = rawJson,
                                uiState = preview.toUiState(
                                    fileName = fileName,
                                    staleAssertionCount = profile
                                        ?.staleRuleAssertionsFor(preview.document)
                                        ?.size
                                        ?: 0,
                                    today = currentLocalDateProvider.today(),
                                ),
                            ),
                            message = null,
                        )
                    }
                },
                onFailure = ::handleImportFailure,
            )
        }
    }

    private fun confirmPendingImport() {
        val pending = transientState.value.pendingImport ?: return
        viewModelScope.launch {
            transientState.update { it.copy(isWorking = true, message = null) }
            opportunityPackRepository.importPack(pending.rawJson).fold(
                onSuccess = { result ->
                    val message = when (result.status) {
                        OpportunityPackImportStatus.IMPORTED -> PackInspectorNotice.Activated(
                            pending.uiState.fileName,
                            pending.uiState.staleAssertionCount,
                        )

                        OpportunityPackImportStatus.NO_CHANGE ->
                            PackInspectorNotice.MatchesActive(pending.uiState.fileName)
                    }
                    transientState.update {
                        it.copy(
                            isWorking = false,
                            selectedOpportunityId = result.document.opportunities.firstOrNull()?.id,
                            diagnostics = emptyList(),
                            pendingImport = null,
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
                pendingImport = null,
                message = PackInspectorNotice.ImportRejected,
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
                        successMessage = PackInspectorNotice.ExportedPack,
                    )
                },
                onFailure = { showFailure(PackInspectorNotice.ExportPackFailed) },
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
                successMessage = PackInspectorNotice.ExportedDiagnostics,
            )
        }
    }

    private suspend fun saveText(
        suggestedName: String,
        extension: String,
        content: String,
        successMessage: PackInspectorNotice,
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
            onFailure = { showFailure(PackInspectorNotice.SaveFailed) },
        )
    }

    private fun showFailure(message: PackInspectorNotice) {
        transientState.update { it.copy(isWorking = false, message = message) }
    }

    private fun buildUiState(
        document: OpportunityPackDocument?,
        loadError: Throwable?,
        transient: PackInspectorTransientState,
        today: LocalDate,
    ): PackInspectorUiState {
        val activePack = document?.toUiState(today)
        val selectedId = transient.selectedOpportunityId
            ?.takeIf { candidate -> activePack?.opportunities?.any { it.id == candidate } == true }
            ?: activePack?.opportunities?.firstOrNull()?.id
        return PackInspectorUiState(
            isLoading = false,
            isWorking = transient.isWorking,
            activePack = activePack,
            selectedOpportunityId = selectedId,
            diagnostics = transient.diagnostics,
            pendingUpdate = transient.pendingImport?.uiState,
            message = transient.message ?: loadError?.let { PackInspectorNotice.LoadFailed },
        )
    }

    private fun OpportunityPackDocument.toUiState(today: LocalDate): ActivePackUiState {
        val dates = opportunities.map(Opportunity::lastCheckedOn).sorted()
        val providers = opportunities.map(Opportunity::provider).distinct().sorted()
        val sourceReviewDueCount = opportunities.count { it.sourceReviewNeedsAttention(today) }
        return ActivePackUiState(
            id = metadata.id,
            name = metadata.name,
            publisher = metadata.publisher,
            versionLabel = "Version ${metadata.version}",
            schemaLabel = "Schema ${metadata.schemaVersion}",
            recordCountLabel = "${opportunities.size} records",
            checkedLabel = "Pack source reviewed on ${metadata.lastCheckedOn}",
            sourceReviewRangeLabel = when {
                dates.isEmpty() -> "No source review dates"
                dates.first() == dates.last() -> "Source records reviewed on ${dates.first()}"
                else -> "Source records reviewed ${dates.first()} – ${dates.last()}"
            },
            sourceReviewStatusLabel = if (sourceReviewDueCount == 0) {
                "All records are within Porta's $DEFAULT_SOURCE_REVIEW_WINDOW_DAYS-day catalog review window."
            } else {
                "$sourceReviewDueCount records need a Porta catalog review. Confirm live sources before acting."
            },
            importedLabel = "Imported ${metadata.importedAtEpochMillis.asLocalizedDate()}",
            sourceUrl = metadata.sourceUrl,
            isBundledPack = bundledPackSource.isBundled(metadata.id),
            providers = providers,
            opportunities = opportunities.map { it.toUiState(today) },
            version = metadata.version,
            schemaVersion = metadata.schemaVersion,
            recordCount = opportunities.size,
            packCheckedOn = metadata.lastCheckedOn,
            earliestRecordCheckedOn = dates.firstOrNull(),
            latestRecordCheckedOn = dates.lastOrNull(),
            sourceReviewDueCount = sourceReviewDueCount,
            importedAtEpochMillis = metadata.importedAtEpochMillis,
        )
    }

    private fun Opportunity.toUiState(today: LocalDate): PackOpportunityUiState = PackOpportunityUiState(
        id = id,
        title = title,
        provider = provider,
        categoryLabel = category.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercaseChar),
        checkedLabel = sourceReviewLabel(today),
        sourceUrl = sourceUrl,
        sourceSection = sourceSection,
        sourceReviewNeedsAttention = sourceReviewNeedsAttention(today),
        ruleNodes = eligibilityRule.flatten(),
        category = category,
        lastCheckedOn = lastCheckedOn,
        sourceFreshness = sourceFreshness(today).status,
    )

    private fun OpportunityPackImportPreview.toUiState(
        fileName: String,
        staleAssertionCount: Int,
        today: LocalDate,
    ): PendingPackUpdateUiState = PendingPackUpdateUiState(
        fileName = fileName,
        packName = document.metadata.name,
        versionTransitionLabel = diff.previousVersion?.let { previous ->
            "Version $previous → ${diff.candidateVersion}"
        } ?: "New pack · version ${diff.candidateVersion}",
        addedCount = diff.addedOpportunityIds.size,
        removedCount = diff.removedOpportunityIds.size,
        changedCount = diff.changedOpportunityIds.size,
        unchangedCount = diff.unchangedOpportunityIds.size,
        staleAssertionCount = staleAssertionCount,
        sourceReviewDueCount = document.opportunities.count { it.sourceReviewNeedsAttention(today) },
        previousVersion = diff.previousVersion,
        candidateVersion = diff.candidateVersion,
    )

    private fun RuleNode.flatten(depth: Int = 0): List<RuleNodeUiState> = when (this) {
        is RuleNode.All -> listOf(
            RuleNodeUiState(
                id = id,
                depth = depth,
                typeLabel = "ALL",
                summary = "Every one of ${children.size} child rules must match.",
                kind = PackRuleKind.ALL,
                childRuleCount = children.size,
            ),
        ) + children.flatMap { it.flatten(depth + 1) }

        is RuleNode.Any -> listOf(
            RuleNodeUiState(
                id = id,
                depth = depth,
                typeLabel = "ANY",
                summary = "At least one of ${children.size} child rules must match.",
                kind = PackRuleKind.ANY,
                childRuleCount = children.size,
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
                kind = PackRuleKind.ATOMIC,
                factKey = factKey,
                operator = operator,
                expectedDisplayValue = expected?.displayValue(),
            ),
        )

        is RuleNode.ManualCheck -> listOf(
            RuleNodeUiState(
                id = id,
                depth = depth,
                typeLabel = "MANUAL CHECK",
                summary = explanation,
                sourceNote = sourceNote,
                kind = PackRuleKind.MANUAL_CHECK,
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
        val pendingImport: PendingPackImport? = null,
        val message: PackInspectorNotice? = null,
    )

    private data class PendingPackImport(
        val rawJson: String,
        val uiState: PendingPackUpdateUiState,
    )
}
