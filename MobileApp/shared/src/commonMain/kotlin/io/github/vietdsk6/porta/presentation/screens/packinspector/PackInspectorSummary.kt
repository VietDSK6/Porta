package io.github.vietdsk6.porta.presentation.screens.packinspector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ButtonStyle
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.pack_activate
import io.github.vietdsk6.porta.generated.resources.pack_active_curated
import io.github.vietdsk6.porta.generated.resources.pack_active_imported
import io.github.vietdsk6.porta.generated.resources.pack_controls
import io.github.vietdsk6.porta.generated.resources.pack_controls_hint
import io.github.vietdsk6.porta.generated.resources.pack_diff_counts
import io.github.vietdsk6.porta.generated.resources.pack_empty_hint
import io.github.vietdsk6.porta.generated.resources.pack_empty_title
import io.github.vietdsk6.porta.generated.resources.pack_export_active
import io.github.vietdsk6.porta.generated.resources.pack_import_another
import io.github.vietdsk6.porta.generated.resources.pack_import_json
import io.github.vietdsk6.porta.generated.resources.pack_install_curated
import io.github.vietdsk6.porta.generated.resources.pack_intro_body
import io.github.vietdsk6.porta.generated.resources.pack_intro_eyebrow
import io.github.vietdsk6.porta.generated.resources.pack_intro_title
import io.github.vietdsk6.porta.generated.resources.pack_keep_current
import io.github.vietdsk6.porta.generated.resources.pack_open_source
import io.github.vietdsk6.porta.generated.resources.pack_providers
import io.github.vietdsk6.porta.generated.resources.pack_published_by
import io.github.vietdsk6.porta.generated.resources.pack_restore_curated
import io.github.vietdsk6.porta.generated.resources.pack_review_title
import io.github.vietdsk6.porta.generated.resources.pack_saved_preserved
import io.github.vietdsk6.porta.generated.resources.pack_source_due
import io.github.vietdsk6.porta.generated.resources.pack_stale_confirmations_count
import io.github.vinceglb.filekit.name
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PackUpdatePreviewDialog(
    pending: PendingPackUpdateUiState,
    isWorking: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag("pack-update-preview"),
        onDismissRequest = onCancel,
        title = { Text(stringResource(Res.string.pack_review_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${pending.packName} · ${pending.localizedVersionTransition()}")
                Text(
                    stringResource(Res.string.pack_diff_counts, pending.addedCount, pending.changedCount, pending.removedCount, pending.unchangedCount),
                )
                if (pending.staleAssertionCount > 0) {
                    Text(
                        pluralStringResource(
                            Res.plurals.pack_stale_confirmations_count,
                            pending.staleAssertionCount,
                            pending.staleAssertionCount,
                        ),
                        color = AppTheme.colors.status.warning,
                    )
                }
                if (pending.sourceReviewDueCount > 0) {
                    Text(
                        stringResource(Res.string.pack_source_due, pending.sourceReviewDueCount),
                        color = AppTheme.colors.status.warning,
                    )
                }
                Text(stringResource(Res.string.pack_saved_preserved))
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("pack-update-confirm"),
                enabled = !isWorking,
                onClick = onConfirm,
            ) {
                Text(stringResource(Res.string.pack_activate))
            }
        },
        dismissButton = {
            TextButton(enabled = !isWorking, onClick = onCancel) { Text(stringResource(Res.string.pack_keep_current)) }
        },
    )
}

@Composable
internal fun InspectorIntro() {
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
                text = stringResource(Res.string.pack_intro_eyebrow),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
            )
            Text(
                text = stringResource(Res.string.pack_intro_title),
                style = AppTheme.typography.h4,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(Res.string.pack_intro_body),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
internal fun LoadingPack() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun EmptyPack(
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
                text = stringResource(Res.string.pack_empty_title),
                style = AppTheme.typography.h5,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.pack_empty_hint),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                text = stringResource(Res.string.pack_import_json),
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-import-empty"),
                enabled = !isWorking,
                onClick = onImportPack,
            )
            AppButton(
                text = stringResource(Res.string.pack_install_curated),
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-install-demo"),
                style = ButtonStyle.ALTERNATIVE,
                enabled = !isWorking,
                onClick = onRestore,
            )
        }
    }
}

@Composable
internal fun ActivePackContent(
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
                        text = if (pack.isBundledPack) stringResource(Res.string.pack_active_curated) else stringResource(Res.string.pack_active_imported),
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
                        text = stringResource(Res.string.pack_published_by, pack.publisher),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.secondary,
                    )
                }
                StatusDot()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetadataPill(pack.localizedVersion())
                MetadataPill(pack.localizedSchema())
                MetadataPill(pack.localizedRecordCount())
            }
            Text(pack.localizedChecked(), color = AppTheme.colors.text.primary, style = AppTheme.typography.bodyMedium)
            Text(
                pack.localizedSourceReviewRange(),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyMedium,
            )
            if (pack.sourceReviewStatusLabel.isNotBlank()) {
                Text(
                    pack.localizedReviewStatus(),
                    color = if (pack.sourceReviewDueCount == 0 ||
                        (pack.sourceReviewDueCount == null && pack.sourceReviewStatusLabel.startsWith("All records"))
                    ) {
                        AppTheme.colors.text.secondary
                    } else {
                        AppTheme.colors.status.warning
                    },
                    style = AppTheme.typography.bodyMedium,
                )
            }
            Text(pack.localizedImported(), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)

            if (pack.providers.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.pack_providers),
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
                    Text(stringResource(Res.string.pack_open_source))
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
                text = stringResource(Res.string.pack_controls),
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.pack_controls_hint),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                text = stringResource(Res.string.pack_import_another),
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-import"),
                enabled = !isWorking,
                onClick = onImportPack,
            )
            AppButton(
                text = stringResource(Res.string.pack_export_active),
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-export"),
                style = ButtonStyle.ALTERNATIVE,
                enabled = !isWorking,
                onClick = onExportPack,
            )
            if (!isBundledPack) {
                AppButton(
                    text = stringResource(Res.string.pack_restore_curated),
                    modifier = Modifier.fillMaxWidth(),
                    style = ButtonStyle.TEXT,
                    enabled = !isWorking,
                    onClick = onRestore,
                )
            }
        }
    }
}
