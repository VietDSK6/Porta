package io.github.vietdsk6.porta.presentation.screens.packinspector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ButtonStyle
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.pack_diagnostics_count
import io.github.vietdsk6.porta.generated.resources.pack_diagnostics_hint
import io.github.vietdsk6.porta.generated.resources.pack_dismiss
import io.github.vietdsk6.porta.generated.resources.pack_export_diagnostics
import io.github.vietdsk6.porta.generated.resources.pack_open_official
import io.github.vietdsk6.porta.generated.resources.pack_records_hint
import io.github.vietdsk6.porta.generated.resources.pack_records_rules
import io.github.vietdsk6.porta.generated.resources.pack_source_note
import io.github.vietdsk6.porta.generated.resources.pack_source_section
import io.github.vietdsk6.porta.presentation.theme.PortaBreakpoints
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OpportunityWorkspace(
    opportunities: List<PackOpportunityUiState>,
    selectedOpportunityId: String?,
    onSelect: (String) -> Unit,
    onOpenSource: (String) -> Unit,
) {
    val selected = opportunities.firstOrNull { it.id == selectedOpportunityId }
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing)) {
        Text(
            text = stringResource(Res.string.pack_records_rules),
            style = AppTheme.typography.h5,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(Res.string.pack_records_hint),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (
                maxWidth >= PortaBreakpoints.wide &&
                LocalDensity.current.fontScale < PortaBreakpoints.maxWideFontScale &&
                selected != null
            ) {
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
                        text = "${opportunity.localizedCategory()} · ${opportunity.localizedChecked()}",
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
                    text = stringResource(Res.string.pack_source_section, it),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            opportunity.ruleNodes.forEach { node ->
                RuleNodeRow(node)
            }
            TextButton(onClick = { onOpenSource(opportunity.sourceUrl) }) {
                Text(stringResource(Res.string.pack_open_official))
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
            color = if (node.kind == PackRuleKind.MANUAL_CHECK ||
                (node.kind == null && node.typeLabel == "MANUAL CHECK")
            ) {
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
                text = node.localizedTypeLabel(),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = node.localizedSummary(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
            )
            node.sourceNote?.let {
                Text(
                    text = stringResource(Res.string.pack_source_note, it),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
        }
    }
}

@Composable
internal fun DiagnosticsCard(
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
                text = stringResource(Res.string.pack_diagnostics_count, diagnostics.size),
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.pack_diagnostics_hint),
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
                text = stringResource(Res.string.pack_export_diagnostics),
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-export-diagnostics"),
                style = ButtonStyle.ALTERNATIVE,
                enabled = !isWorking,
                onClick = onExport,
            )
        }
    }
}

@Composable
internal fun MessageNotice(
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
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.pack_dismiss)) }
        }
    }
}
