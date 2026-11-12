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
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.frontier_plot_accessibility
import io.github.vietdsk6.porta.generated.resources.frontier_plot_axis_days
import io.github.vietdsk6.porta.generated.resources.frontier_plot_axis_doors
import io.github.vietdsk6.porta.generated.resources.frontier_plot_hint
import io.github.vietdsk6.porta.generated.resources.frontier_plot_metrics
import io.github.vietdsk6.porta.generated.resources.frontier_plot_mixed_scopes
import io.github.vietdsk6.porta.generated.resources.frontier_plot_point_shared_a11y
import io.github.vietdsk6.porta.generated.resources.frontier_plot_point_single_a11y
import io.github.vietdsk6.porta.generated.resources.frontier_plot_shared_title
import io.github.vietdsk6.porta.generated.resources.frontier_plot_single_title
import io.github.vietdsk6.porta.generated.resources.frontier_tradeoff_title
import io.github.vietdsk6.porta.presentation.theme.PortaColors
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FrontierPlot(
    actions: List<FrontierActionUiState>,
    horizonDays: Int,
    selectedActionId: String?,
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val plotGroups = actions.toPlotGroups()
    if (plotGroups.size < 2) return

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
                Text(stringResource(Res.string.frontier_tradeoff_title), style = AppTheme.typography.h6)
                Text(
                    text = stringResource(Res.string.frontier_plot_hint),
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodySmall,
                )
            }
            ImpactPlotArea(
                groups = plotGroups,
                horizonDays = horizonDays,
                selectedActionId = selectedActionId,
                onActionSelected = onActionSelected,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(plotGroups.map { it.representative.cost }.distinct()) { cost -> CostLegendItem(cost) }
            }
            plotGroups.forEachIndexed { index, group ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (group.actions.size == 1) {
                            stringResource(Res.string.frontier_plot_single_title, index + 1, group.representative.titleText())
                        } else {
                            stringResource(Res.string.frontier_plot_shared_title, index + 1, group.actions.size)
                        },
                        color = if (group.contains(selectedActionId)) {
                            AppTheme.colors.primary
                        } else {
                            AppTheme.colors.text.secondary
                        },
                        style = AppTheme.typography.bodySmall,
                        fontWeight = if (group.contains(selectedActionId)) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                    )
                    Text(
                        text = stringResource(
                            Res.string.frontier_plot_metrics,
                            group.representative.durationDays,
                            group.representative.improvedCount,
                            group.representative.costText(),
                        ),
                        color = AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodySmall,
                    )
                    if (group.actions.size > 1) {
                        Text(
                            text = if (group.actions.map { it.scope }.distinct().size > 1) {
                                stringResource(Res.string.frontier_plot_mixed_scopes)
                            } else {
                                group.representative.scopeText()
                            },
                            color = AppTheme.colors.text.secondary,
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpactPlotArea(
    groups: List<FrontierPlotGroup>,
    horizonDays: Int,
    selectedActionId: String?,
    onActionSelected: (String) -> Unit,
) {
    val axisColor = AppTheme.colors.outline.copy(alpha = 0.55f)
    val gridColor = AppTheme.colors.outline.copy(alpha = 0.18f)
    val maxImpact = groups.maxOfOrNull { it.representative.improvedCount }?.coerceAtLeast(1) ?: 1
    val plotDescription = stringResource(Res.string.frontier_plot_accessibility)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(PLOT_HEIGHT)
            .testTag("frontier-impact-map-plot")
            .semantics {
                contentDescription = plotDescription
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
            text = stringResource(Res.string.frontier_plot_axis_doors, maxImpact),
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
            text = stringResource(Res.string.frontier_plot_axis_days, horizonDays),
            modifier = Modifier.offset(x = maxWidth - 70.dp, y = PLOT_HEIGHT - 30.dp),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )

        groups.forEachIndexed { index, group ->
            val action = group.representative
            val xFraction = (action.durationDays.toFloat() / horizonDays.coerceAtLeast(1)).coerceIn(0f, 1f)
            val yFraction = (action.improvedCount.toFloat() / maxImpact).coerceIn(0f, 1f)
            val markerX = PLOT_LEFT + (plotWidth * xFraction) - (POINT_SIZE / 2)
            val markerY = PLOT_TOP + (plotHeight * (1f - yFraction)) - (POINT_SIZE / 2)
            val isSelected = group.contains(selectedActionId)
            val selectedGroupActionId = selectedActionId
                ?.takeIf(group::contains)
                ?: action.id
            val pointDescription = if (group.actions.size == 1) {
                stringResource(
                    Res.string.frontier_plot_point_single_a11y,
                    action.titleText(),
                    action.durationDays,
                    action.improvedCount,
                    action.costText(),
                )
            } else {
                stringResource(
                    Res.string.frontier_plot_point_shared_a11y,
                    group.actions.size,
                    action.durationDays,
                    action.improvedCount,
                    action.costText(),
                )
            }
            Surface(
                modifier = Modifier
                    .offset(x = markerX, y = markerY)
                    .size(POINT_SIZE)
                    .testTag("frontier-plot-point-${action.id}")
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onActionSelected(selectedGroupActionId) },
                    )
                    .semantics {
                        contentDescription = pointDescription
                    },
                shape = CircleShape,
                color = PortaColors.cost(action.cost),
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
                        text = if (group.actions.size == 1) "${index + 1}" else "${group.actions.size}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

private data class FrontierPlotGroup(
    val actions: List<FrontierActionUiState>,
) {
    val representative: FrontierActionUiState
        get() = actions.first()

    fun contains(actionId: String?): Boolean = actions.any { it.id == actionId }
}

private data class FrontierPlotVector(
    val durationDays: Int,
    val improvedCount: Int,
    val cost: CostBand,
)

private fun List<FrontierActionUiState>.toPlotGroups(): List<FrontierPlotGroup> = groupBy { action ->
    FrontierPlotVector(
        durationDays = action.durationDays,
        improvedCount = action.improvedCount,
        cost = action.cost,
    )
}.values.map(::FrontierPlotGroup)

@Composable
private fun CostLegendItem(cost: CostBand) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = PortaColors.cost(cost)) {}
        Text(cost.frontierCostText(), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
    }
}

private val PLOT_HEIGHT = 210.dp
private val PLOT_LEFT = 42.dp
private val PLOT_RIGHT = 18.dp
private val PLOT_TOP = 28.dp
private val PLOT_BOTTOM = 42.dp
private val POINT_SIZE = 44.dp
