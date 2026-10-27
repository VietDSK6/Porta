package io.github.vietdsk6.porta.presentation.components.porta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.components.porta.PortaArch
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
)

/**
 * A restrained, data-driven view of one action affecting one or more opportunities.
 * It never infers nodes or labels: callers provide every displayed result.
 */
@Composable
fun ActionImpactDiagram(
    actionLabel: String,
    actualLabel: String,
    simulatedLabel: String,
    nodes: List<ActionImpactNodeUiState>,
    modifier: Modifier = Modifier,
) {
    if (nodes.isEmpty()) return
    Box(
        modifier = modifier.fillMaxWidth().testTag("action-impact-diagram"),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (nodes.size == 1) "ONE CHECKED PATH" else "IMPACT PATH · ${nodes.size} DOORS",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
            )
            PortaArch(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .heightIn(min = 132.dp),
                containerColor = AppTheme.colors.alternative,
                outlineColor = AppTheme.colors.primary,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp),
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
            val connectorColor = AppTheme.colors.outline
            Canvas(Modifier.width(2.dp).height(18.dp)) {
                drawLine(
                    color = connectorColor,
                    start = androidx.compose.ui.geometry.Offset(size.width / 2f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height),
                    strokeWidth = size.width,
                    cap = StrokeCap.Round,
                )
            }
            nodes.forEachIndexed { index, node ->
                val position = when {
                    nodes.size == 1 -> PortaConnectorPosition.SINGLE
                    index == 0 -> PortaConnectorPosition.FIRST
                    index == nodes.lastIndex -> PortaConnectorPosition.LAST
                    else -> PortaConnectorPosition.MIDDLE
                }
                ImpactNode(
                    node = node,
                    position = position,
                    modifier = Modifier.fillMaxWidth().testTag("action-impact-node-${node.id}"),
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
        modifier = modifier.height(IntrinsicSize.Min).heightIn(min = 82.dp),
        verticalAlignment = Alignment.Top,
    ) {
        PortaConnectorPath(
            position = position,
            color = AppTheme.colors.outline,
            modifier = Modifier.width(38.dp).fillMaxHeight(),
        )
        Surface(
            modifier = Modifier
                .weight(1f)
                .semantics { stateDescription = node.statusLabel },
            color = containerColor,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.42f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PortaArch(
                    modifier = Modifier.size(width = 34.dp, height = 42.dp),
                    containerColor = Color.Transparent,
                    outlineColor = statusColor,
                    borderWidth = 1.5.dp,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = node.title,
                        color = AppTheme.colors.text.primary,
                        style = AppTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
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
                }
            }
        }
    }
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
