package io.github.vietdsk6.porta.presentation.screens.packinspector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ButtonStyle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.util.Platform
import io.github.vietdsk6.porta.util.getPlatform
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.launch

@Composable
fun PackInspectorScreen(
    viewModel: PackInspectorViewModel,
    onNavigateBack: () -> Unit,
    onOpenOpportunityStudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val packPicker = rememberFilePickerLauncher(
        type = FileKitType.File("json"),
        mode = FileKitMode.Single,
    ) { file ->
        file ?: return@rememberFilePickerLauncher
        coroutineScope.launch {
            runCatching { file.readString() }.fold(
                onSuccess = { rawJson ->
                    viewModel.onUiEvent(PackInspectorUiEvent.ImportPack(file.name, rawJson))
                },
                onFailure = {
                    viewModel.onUiEvent(PackInspectorUiEvent.ImportReadFailed)
                },
            )
        }
    }

    PackInspectorScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onImportPack = packPicker::launch,
        onOpenSource = uriHandler::openUri,
        supportsOpportunityStudio = getPlatform() == Platform.Desktop,
        onOpenOpportunityStudio = onOpenOpportunityStudio,
    )
}

@Composable
fun PackInspectorScreen(
    uiState: PackInspectorUiState,
    onUiEvent: (PackInspectorUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onImportPack: () -> Unit,
    onOpenSource: (String) -> Unit,
    supportsOpportunityStudio: Boolean = false,
    onOpenOpportunityStudio: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("pack-inspector-screen"),
        isScrollableContent = true,
        title = "Pack inspector",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            InspectorIntro()

            if (supportsOpportunityStudio) {
                OpportunityStudioEntry(onOpenOpportunityStudio)
            }

            when {
                uiState.isLoading -> LoadingPack()

                uiState.activePack == null -> EmptyPack(
                    isWorking = uiState.isWorking,
                    onImportPack = onImportPack,
                    onRestore = { onUiEvent(PackInspectorUiEvent.RestoreBundledRequested) },
                )

                else -> ActivePackContent(
                    uiState = uiState,
                    onUiEvent = onUiEvent,
                    onImportPack = onImportPack,
                    onOpenSource = onOpenSource,
                )
            }

            if (uiState.diagnostics.isNotEmpty()) {
                DiagnosticsCard(
                    diagnostics = uiState.diagnostics,
                    isWorking = uiState.isWorking,
                    onExport = { onUiEvent(PackInspectorUiEvent.ExportDiagnostics) },
                )
            }

            uiState.message?.let { message ->
                MessageNotice(
                    message = message,
                    onDismiss = { onUiEvent(PackInspectorUiEvent.MessageHandled) },
                )
            }
        }
    }

    uiState.pendingUpdate?.let { pending ->
        PackUpdatePreviewDialog(
            pending = pending,
            isWorking = uiState.isWorking,
            onConfirm = { onUiEvent(PackInspectorUiEvent.ImportPreviewConfirmed) },
            onCancel = { onUiEvent(PackInspectorUiEvent.ImportPreviewCancelled) },
        )
    }
}

@Composable
private fun OpportunityStudioEntry(onOpenOpportunityStudio: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "DESKTOP AUTHORING LAB",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
            )
            Text(
                text = "Build a sourced opportunity draft",
                style = AppTheme.typography.h5,
                color = AppTheme.colors.text.primary,
            )
            Text(
                text = "Create ALL or ANY eligibility rules with a form, validate the complete pack, then export JSON. " +
                    "The active catalog changes only after you return here and approve its import diff.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-open-studio"),
                text = "Open Opportunity Studio",
                onClick = onOpenOpportunityStudio,
            )
        }
    }
}

@Composable
private fun PackUpdatePreviewDialog(
    pending: PendingPackUpdateUiState,
    isWorking: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag("pack-update-preview"),
        onDismissRequest = onCancel,
        title = { Text("Review catalog changes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${pending.packName} · ${pending.versionTransitionLabel}")
                Text(
                    "${pending.addedCount} added · ${pending.changedCount} changed · " +
                        "${pending.removedCount} removed · ${pending.unchangedCount} unchanged",
                )
                if (pending.staleAssertionCount > 0) {
                    Text(
                        "${pending.staleAssertionCount} programme confirmation(s) will no longer match the new rule fingerprint. " +
                            "They stay stored for audit but will not satisfy the updated checks.",
                        color = AppTheme.colors.status.warning,
                    )
                }
                if (pending.sourceReviewDueCount > 0) {
                    Text(
                        "${pending.sourceReviewDueCount} candidate source record(s) are outside Porta's review window or need date attention.",
                        color = AppTheme.colors.status.warning,
                    )
                }
                Text("Saved doors and your academic profile are preserved.")
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("pack-update-confirm"),
                enabled = !isWorking,
                onClick = onConfirm,
            ) {
                Text("Activate pack")
            }
        },
        dismissButton = {
            TextButton(enabled = !isWorking, onClick = onCancel) { Text("Keep current pack") }
        },
    )
}

@Composable
private fun InspectorIntro() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "READ-ONLY DATA WORKSPACE",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
            )
            Text(
                text = "Trust the rules you can inspect",
                style = AppTheme.typography.h4,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "Every import is validated as one atomic pack, then paused for a change preview. " +
                    "Invalid or unapproved files never replace the active catalog.",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
private fun LoadingPack() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyPack(
    isWorking: Boolean,
    onImportPack: () -> Unit,
    onRestore: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Text(
                text = "No active opportunity pack",
                style = AppTheme.typography.h5,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Import a versioned JSON pack, or install Porta's bundled curated catalog.",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                text = "Import JSON pack",
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-import-empty"),
                enabled = !isWorking,
                onClick = onImportPack,
            )
            AppButton(
                text = "Install curated pack",
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-install-demo"),
                style = ButtonStyle.ALTERNATIVE,
                enabled = !isWorking,
                onClick = onRestore,
            )
        }
    }
}

@Composable
private fun ActivePackContent(
    uiState: PackInspectorUiState,
    onUiEvent: (PackInspectorUiEvent) -> Unit,
    onImportPack: () -> Unit,
    onOpenSource: (String) -> Unit,
) {
    val pack = checkNotNull(uiState.activePack)
    PackSummaryCard(pack = pack, onOpenSource = onOpenSource)
    PackControls(
        isWorking = uiState.isWorking,
        isBundledPack = pack.isBundledPack,
        onImportPack = onImportPack,
        onExportPack = { onUiEvent(PackInspectorUiEvent.ExportActivePack) },
        onRestore = { onUiEvent(PackInspectorUiEvent.RestoreBundledRequested) },
    )
    OpportunityWorkspace(
        opportunities = pack.opportunities,
        selectedOpportunityId = uiState.selectedOpportunityId,
        onSelect = { onUiEvent(PackInspectorUiEvent.OpportunitySelected(it)) },
        onOpenSource = onOpenSource,
    )
}

@Composable
private fun PackSummaryCard(
    pack: ActivePackUiState,
    onOpenSource: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (pack.isBundledPack) "ACTIVE · CURATED BUNDLE" else "ACTIVE · IMPORTED PACK",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = pack.name,
                        style = AppTheme.typography.h5,
                        color = AppTheme.colors.text.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Published by ${pack.publisher}",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.secondary,
                    )
                }
                StatusDot()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetadataPill(pack.versionLabel)
                MetadataPill(pack.schemaLabel)
                MetadataPill(pack.recordCountLabel)
            }
            Text(pack.checkedLabel, color = AppTheme.colors.text.primary, style = AppTheme.typography.bodyMedium)
            Text(
                pack.sourceReviewRangeLabel,
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyMedium,
            )
            if (pack.sourceReviewStatusLabel.isNotBlank()) {
                Text(
                    pack.sourceReviewStatusLabel,
                    color = if (pack.sourceReviewStatusLabel.startsWith("All records")) {
                        AppTheme.colors.text.secondary
                    } else {
                        AppTheme.colors.status.warning
                    },
                    style = AppTheme.typography.bodyMedium,
                )
            }
            Text(pack.importedLabel, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)

            if (pack.providers.isNotEmpty()) {
                Text(
                    text = "Providers represented",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = pack.providers.joinToString(),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.primary,
                )
            }
            pack.sourceUrl?.let { sourceUrl ->
                TextButton(onClick = { onOpenSource(sourceUrl) }) {
                    Text("Open pack source")
                }
            }
        }
    }
}

@Composable
private fun MetadataPill(text: String) {
    Surface(
        color = AppTheme.colors.alternative,
        shape = CircleShape,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.onAlternative,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusDot() {
    Surface(
        modifier = Modifier.size(14.dp),
        color = AppTheme.colors.status.success,
        shape = CircleShape,
    ) {}
}

@Composable
private fun PackControls(
    isWorking: Boolean,
    isBundledPack: Boolean,
    onImportPack: () -> Unit,
    onExportPack: () -> Unit,
    onRestore: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Text(
                text = "Pack controls",
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Imports replace the active catalog only after validation and your review of the exact diff.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                text = "Import another JSON pack",
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-import"),
                enabled = !isWorking,
                onClick = onImportPack,
            )
            AppButton(
                text = "Export active pack",
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-export"),
                style = ButtonStyle.ALTERNATIVE,
                enabled = !isWorking,
                onClick = onExportPack,
            )
            if (!isBundledPack) {
                AppButton(
                    text = "Restore bundled curated pack",
                    modifier = Modifier.fillMaxWidth(),
                    style = ButtonStyle.TEXT,
                    enabled = !isWorking,
                    onClick = onRestore,
                )
            }
        }
    }
}

@Composable
private fun OpportunityWorkspace(
    opportunities: List<PackOpportunityUiState>,
    selectedOpportunityId: String?,
    onSelect: (String) -> Unit,
    onOpenSource: (String) -> Unit,
) {
    val selected = opportunities.firstOrNull { it.id == selectedOpportunityId }
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
        Text(
            text = "Records and typed rules",
            style = AppTheme.typography.h5,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Select a record to inspect the exact rule tree Porta evaluates.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth >= 840.dp && selected != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
                    verticalAlignment = Alignment.Top,
                ) {
                    OpportunityList(
                        opportunities = opportunities,
                        selectedOpportunityId = selectedOpportunityId,
                        onSelect = onSelect,
                        modifier = Modifier.weight(0.42f),
                    )
                    RuleTreeCard(
                        opportunity = selected,
                        onOpenSource = onOpenSource,
                        modifier = Modifier.weight(0.58f),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
                    OpportunityList(
                        opportunities = opportunities,
                        selectedOpportunityId = selectedOpportunityId,
                        onSelect = onSelect,
                    )
                    selected?.let {
                        RuleTreeCard(opportunity = it, onOpenSource = onOpenSource)
                    }
                }
            }
        }
    }
}

@Composable
private fun OpportunityList(
    opportunities: List<PackOpportunityUiState>,
    selectedOpportunityId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        opportunities.forEach { opportunity ->
            val isSelected = opportunity.id == selectedOpportunityId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pack-inspector-opportunity-${opportunity.id}")
                    .semantics { selected = isSelected }
                    .clickable(role = Role.RadioButton) { onSelect(opportunity.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        AppTheme.colors.alternative
                    } else {
                        AppTheme.colors.surfaceContainer
                    },
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) {
                        AppTheme.colors.primary
                    } else {
                        AppTheme.colors.outline.copy(alpha = 0.4f)
                    },
                ),
            ) {
                Column(
                    modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = opportunity.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.text.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = opportunity.provider,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.secondary,
                    )
                    Text(
                        text = "${opportunity.categoryLabel} · ${opportunity.checkedLabel}",
                        style = AppTheme.typography.bodySmall,
                        color = if (opportunity.sourceReviewNeedsAttention) {
                            AppTheme.colors.status.warning
                        } else {
                            AppTheme.colors.primary
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleTreeCard(
    opportunity: PackOpportunityUiState,
    onOpenSource: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Text(
                text = opportunity.title,
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            opportunity.sourceSection?.let {
                Text(
                    text = "Source section: $it",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            opportunity.ruleNodes.forEach { node ->
                RuleNodeRow(node)
            }
            TextButton(onClick = { onOpenSource(opportunity.sourceUrl) }) {
                Text("Open official source")
            }
        }
    }
}

@Composable
private fun RuleNodeRow(node: RuleNodeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = (node.depth * 18).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            modifier = Modifier.padding(top = 7.dp).size(8.dp),
            color = if (node.typeLabel == "MANUAL CHECK") {
                AppTheme.colors.status.warning
            } else {
                AppTheme.colors.primary
            },
            shape = CircleShape,
        ) {}
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = node.typeLabel,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = node.summary,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
            )
            node.sourceNote?.let {
                Text(
                    text = "Source note: $it",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
        }
    }
}

@Composable
private fun DiagnosticsCard(
    diagnostics: List<PackDiagnosticUiState>,
    isWorking: Boolean,
    onExport: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.status.errorContainer),
        border = BorderStroke(1.dp, AppTheme.colors.status.error.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Text(
                text = "Import diagnostics · ${diagnostics.size}",
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "The candidate pack was rejected as a whole. These paths point to what must be corrected.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            diagnostics.forEach { diagnostic ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${diagnostic.severity} · ${diagnostic.code}",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.status.error,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = diagnostic.path,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    Text(
                        text = diagnostic.message,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.primary,
                    )
                }
            }
            AppButton(
                text = "Export diagnostics",
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-export-diagnostics"),
                style = ButtonStyle.ALTERNATIVE,
                enabled = !isWorking,
                onClick = onExport,
            )
        }
    }
}

@Composable
private fun MessageNotice(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
            )
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Preview
@Composable
private fun PackInspectorScreenPreview() {
    AppTheme {
        PackInspectorScreen(
            uiState = previewState,
            onUiEvent = {},
            onNavigateBack = {},
            onImportPack = {},
            onOpenSource = {},
        )
    }
}

@Preview(device = "spec:width=1200dp,height=800dp,dpi=160")
@Composable
private fun PackInspectorDesktopPreview() {
    AppTheme {
        PackInspectorScreen(
            uiState = previewState,
            onUiEvent = {},
            onNavigateBack = {},
            onImportPack = {},
            onOpenSource = {},
        )
    }
}

private val previewState = PackInspectorUiState(
    isLoading = false,
    activePack = ActivePackUiState(
        id = "porta-curated-opportunities-2026-10-21",
        name = "Porta curated opportunity pack",
        publisher = "Porta · independently curated from official sources",
        versionLabel = "Version 1",
        schemaLabel = "Schema 1",
        recordCountLabel = "15 records",
        checkedLabel = "Pack checked 2026-10-21",
        sourceReviewRangeLabel = "Source records reviewed on 2026-10-21",
        importedLabel = "Imported Oct 21, 2026",
        sourceUrl = null,
        isBundledPack = true,
        providers = listOf("CERN openlab", "Kotlin Foundation", "UNICEF"),
        opportunities = listOf(
            PackOpportunityUiState(
                id = "kotlin-student-coding-competition-2026",
                title = "Kotlin Student Coding Competition",
                provider = "Kotlin Foundation",
                categoryLabel = "Student contest",
                checkedLabel = "Checked 2026-10-21",
                sourceUrl = "https://kotlinconf.com/contest-rules/",
                sourceSection = "Eligibility; How to Enter",
                ruleNodes = listOf(
                    RuleNodeUiState(
                        id = "kotlin-root",
                        depth = 0,
                        typeLabel = "ALL",
                        summary = "Every one of 3 child rules must match.",
                    ),
                ),
            ),
            PackOpportunityUiState(
                id = "unicef-internship-rolling",
                title = "UNICEF Internship Programme",
                provider = "UNICEF",
                categoryLabel = "Internship",
                checkedLabel = "Checked 2026-10-21",
                sourceUrl = "https://www.unicef.org/careers/internships",
                sourceSection = "Requirements; How to apply",
                ruleNodes = emptyList(),
            ),
        ),
    ),
    selectedOpportunityId = "kotlin-student-coding-competition-2026",
)
