package io.github.vietdsk6.porta.presentation.screens.frontier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.CostBand

@Composable
internal fun FrontierPlot(
    actions: List<FrontierActionUiState>,
    horizonDays: Int,
    selectedActionId: String?,
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (actions.size < 2) return

    Card(
        modifier = modifier.fillMaxWidth().testTag("frontier-impact-map"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Compare trade-offs", style = AppTheme.typography.h6)
                Text(
                    text = "Up and left means more checked requirements affected in less time.",
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
            }
            ImpactPlotArea(
                actions = actions,
                horizonDays = horizonDays,
                selectedActionId = selectedActionId,
                onActionSelected = onActionSelected,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(CostBand.entries) { cost -> CostLegendItem(cost) }
            }
            actions.forEachIndexed { index, action ->
                Text(
                    text = "${index + 1}. ${action.title} · ${action.durationDays} days · " +
                        "${action.improvedCount} doors",
                    color = if (action.id == selectedActionId) {
                        AppTheme.colors.primary
                    } else {
                        AppTheme.colors.text.secondary
                    },
                    style = AppTheme.typography.bodySmall,
                    fontWeight = if (action.id == selectedActionId) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ImpactPlotArea(
    actions: List<FrontierActionUiState>,
    horizonDays: Int,
    selectedActionId: String?,
    onActionSelected: (String) -> Unit,
) {
    val axisColor = AppTheme.colors.outline.copy(alpha = 0.55f)
    val gridColor = AppTheme.colors.outline.copy(alpha = 0.18f)
    val maxImpact = actions.maxOfOrNull(FrontierActionUiState::improvedCount)?.coerceAtLeast(1) ?: 1

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(PLOT_HEIGHT)
            .testTag("frontier-impact-map-plot")
            .semantics {
                contentDescription =
                    "Action impact map. Horizontal axis is duration in days. " +
                    "Vertical axis is affected doors. Point color is cost and every point has a text label."
            },
    ) {
        val plotWidth = (maxWidth - PLOT_LEFT - PLOT_RIGHT).coerceAtLeast(1.dp)
        val plotHeight = PLOT_HEIGHT - PLOT_TOP - PLOT_BOTTOM

        Canvas(modifier = Modifier.fillMaxSize()) {
            val left = PLOT_LEFT.toPx()
            val top = PLOT_TOP.toPx()
            val right = size.width - PLOT_RIGHT.toPx()
            val bottom = size.height - PLOT_BOTTOM.toPx()
            drawLine(
                color = axisColor,
                start = Offset(left, top),
                end = Offset(left, bottom),
            )
            drawLine(
                color = axisColor,
                start = Offset(left, bottom),
                end = Offset(right, bottom),
            )
            repeat(4) { index ->
                val fraction = (index + 1) / 4f
                val y = bottom - ((bottom - top) * fraction)
                drawLine(
                    gridColor,
                    start = Offset(left, y),
                    end = Offset(right, y),
                )
            }
        }

        Text(
            text = "$maxImpact doors",
            modifier = Modifier.offset(x = 0.dp, y = 0.dp),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
        Text(
            text = "0",
            modifier = Modifier.offset(x = 18.dp, y = PLOT_HEIGHT - 30.dp),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
        Text(
            text = "$horizonDays days",
            modifier = Modifier.offset(x = maxWidth - 70.dp, y = PLOT_HEIGHT - 30.dp),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )

        actions.forEachIndexed { index, action ->
            val xFraction = (action.durationDays.toFloat() / horizonDays.coerceAtLeast(1)).coerceIn(0f, 1f)
            val yFraction = (action.improvedCount.toFloat() / maxImpact).coerceIn(0f, 1f)
            val markerX = PLOT_LEFT + (plotWidth * xFraction) - (POINT_SIZE / 2)
            val markerY = PLOT_TOP + (plotHeight * (1f - yFraction)) - (POINT_SIZE / 2)
            val isSelected = action.id == selectedActionId
            Surface(
                modifier = Modifier
                    .offset(x = markerX, y = markerY)
                    .size(POINT_SIZE)
                    .testTag("frontier-plot-point-${action.id}")
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onActionSelected(action.id) },
                    )
                    .semantics {
                        contentDescription =
                            "${action.title}. ${action.durationDays} days, ${action.improvedCount} affected doors, " +
                            "${action.costLabel} cost."
                    },
                shape = CircleShape,
                color = action.cost.color(),
                border = BorderStroke(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) AppTheme.colors.text.primary else AppTheme.colors.background,
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CostLegendItem(cost: CostBand) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = cost.color()) {}
        Text(cost.readable(), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
    }
}

private fun CostBand.color(): Color = when (this) {
    CostBand.FREE -> Color(0xFF0F9D8A)
    CostBand.LOW -> Color(0xFF3976D9)
    CostBand.MEDIUM -> Color(0xFFD18A00)
    CostBand.HIGH -> Color(0xFFC94B56)
    CostBand.UNKNOWN -> Color(0xFF74717D)
}

private fun CostBand.readable(): String = name.lowercase().replaceFirstChar(Char::uppercaseChar)

private val PLOT_HEIGHT = 210.dp
private val PLOT_LEFT = 42.dp
private val PLOT_RIGHT = 18.dp
private val PLOT_TOP = 28.dp
private val PLOT_BOTTOM = 42.dp
private val POINT_SIZE = 44.dp
