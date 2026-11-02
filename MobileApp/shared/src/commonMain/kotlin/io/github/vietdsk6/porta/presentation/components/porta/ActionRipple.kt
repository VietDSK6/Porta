package io.github.vietdsk6.porta.presentation.components.porta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.components.porta.PortaConnectorPath
import io.github.vietdsk6.porta.designsystem.components.porta.PortaConnectorPosition
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

enum class ImpactOutcome {
    CURRENT_CHECKS_PASSED,
    CHECKED_PROGRESS,
    NEEDS_VERIFICATION,
}

@Immutable
data class ActionImpactNodeUiState(
    val id: String,
    val title: String,
    val statusLabel: String,
    val transitionLabel: String,
    val outcome: ImpactOutcome,
    val timingWarning: String? = null,
)

/**
 * A restrained, data-driven view of one action affecting one or more opportunities.
 * It never infers nodes or labels: callers provide every displayed result.
 */
@Composable
fun ActionRipple(
    actionLabel: String,
    actualLabel: String,
    simulatedLabel: String,
    nodes: List<ActionImpactNodeUiState>,
    overflowDestination: String,
    modifier: Modifier = Modifier,
) {
    if (nodes.isEmpty()) return
    val visibleNodes = nodes.take(MAX_VISIBLE_NODES)
    val overflowCount = nodes.size - visibleNodes.size
    Box(
        modifier = modifier.fillMaxWidth().testTag("action-ripple"),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (nodes.size == 1) "ACTION RIPPLE · 1 OPPORTUNITY" else "ACTION RIPPLE · ${nodes.size} OPPORTUNITIES",
                modifier = Modifier.fillMaxWidth(),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                color = AppTheme.colors.alternative,
                shape = RoundedCornerShape(topStart = 54.dp, topEnd = 54.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.55f)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = actionLabel,
                        color = AppTheme.colors.text.primary,
                        style = AppTheme.typography.h5,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "$actualLabel → $simulatedLabel",
                        color = AppTheme.colors.onAlternative,
                        style = AppTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Scenario only · saved profile unchanged",
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodySmall,
                    )
                }
            }
            PortaActionPathGlyph(
                semanticLabel = "Action connects to ${nodes.size} assessed opportunities",
                color = AppTheme.colors.outline,
                modifier = Modifier.size(width = 24.dp, height = 18.dp),
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                visibleNodes.forEachIndexed { index, node ->
                    ImpactNode(
                        node = node,
                        position = when {
                            visibleNodes.size == 1 -> PortaConnectorPosition.SINGLE
                            index == 0 -> PortaConnectorPosition.FIRST
                            index == visibleNodes.lastIndex -> PortaConnectorPosition.LAST
                            else -> PortaConnectorPosition.MIDDLE
                        },
                        modifier = Modifier.fillMaxWidth().testTag("action-impact-node-${node.id}"),
                    )
                }
            }
            if (overflowCount > 0) {
                Text(
                    text = "+$overflowCount more in $overflowDestination",
                    modifier = Modifier.fillMaxWidth().testTag("action-ripple-overflow"),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ImpactNode(
    node: ActionImpactNodeUiState,
    position: PortaConnectorPosition,
    modifier: Modifier = Modifier,
) {
    val statusColor = node.outcome.statusColor()
    val containerColor = node.outcome.containerColor()
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PortaConnectorPath(
            position = position,
            color = AppTheme.colors.outline,
            modifier = Modifier.width(32.dp).fillMaxHeight(),
        )
        Surface(
            modifier = Modifier.weight(1f).padding(vertical = 4.dp).semantics {
                stateDescription = node.statusLabel
                contentDescription = listOfNotNull(
                    node.title,
                    node.transitionLabel,
                    node.statusLabel,
                    node.timingWarning,
                ).joinToString(". ")
            },
            color = containerColor,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.42f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PortaStatusGlyph(
                    semanticLabel = node.statusLabel,
                    shape = node.outcome.glyphShape(),
                    color = statusColor,
                    modifier = Modifier.size(28.dp),
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = node.title,
                        color = AppTheme.colors.text.primary,
                        style = AppTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = node.transitionLabel,
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodySmall,
                    )
                    Text(
                        text = node.statusLabel,
                        color = statusColor,
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                    )
                    node.timingWarning?.let { warning ->
                        Text(
                            text = warning,
                            color = AppTheme.colors.status.warning,
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

private fun ImpactOutcome.glyphShape(): PortaStatusGlyphShape = when (this) {
    ImpactOutcome.CURRENT_CHECKS_PASSED -> PortaStatusGlyphShape.CHECK
    ImpactOutcome.CHECKED_PROGRESS -> PortaStatusGlyphShape.ACTION
    ImpactOutcome.NEEDS_VERIFICATION -> PortaStatusGlyphShape.VERIFY
}

@Composable
private fun ImpactOutcome.statusColor(): Color = when (this) {
    ImpactOutcome.CURRENT_CHECKS_PASSED -> AppTheme.colors.status.success
    ImpactOutcome.CHECKED_PROGRESS -> AppTheme.colors.primary
    ImpactOutcome.NEEDS_VERIFICATION -> AppTheme.colors.status.warning
}

@Composable
private fun ImpactOutcome.containerColor(): Color = when (this) {
    ImpactOutcome.CURRENT_CHECKS_PASSED -> AppTheme.colors.status.successContainer
    ImpactOutcome.CHECKED_PROGRESS -> AppTheme.colors.alternative
    ImpactOutcome.NEEDS_VERIFICATION -> AppTheme.colors.status.warningContainer
}

private const val MAX_VISIBLE_NODES = 5

@Preview
@Composable
private fun ActionRippleLightPreview() {
    ActionRipplePreview(isDarkMode = false)
}

@Preview
@Composable
private fun ActionRippleDarkPreview() {
    ActionRipplePreview(isDarkMode = true)
}

@Preview(widthDp = 320, fontScale = 2f)
@Composable
private fun ActionRippleLargeTextPreview() {
    CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
        Box(Modifier.width(320.dp)) { ActionRipplePreview(isDarkMode = false) }
    }
}

@Composable
private fun ActionRipplePreview(isDarkMode: Boolean) {
    AppTheme(isDarkMode = isDarkMode) {
        ActionRipple(
            actionLabel = "Raise IELTS to 7.0",
            actualLabel = "6.5 band",
            simulatedLabel = "7.0 band",
            nodes = listOf(
                ActionImpactNodeUiState("aurora", "Aurora Scholars", "Would pass current checks", "Needs action → Assessed rules passed", ImpactOutcome.CURRENT_CHECKS_PASSED),
                ActionImpactNodeUiState("erasmus", "Erasmus Mundus Joint Masters", "Still needs information or source review", "Needs action → Needs information", ImpactOutcome.NEEDS_VERIFICATION, "Deadline needs confirmation"),
                ActionImpactNodeUiState("forge", "Forge Student Lab", "Would address a checked requirement", "Needs action → Needs action", ImpactOutcome.CHECKED_PROGRESS),
            ),
            overflowDestination = "Results",
        )
    }
}
