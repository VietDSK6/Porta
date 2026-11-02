package io.github.vietdsk6.porta.presentation.components.porta

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

/** A graphic's label must come from the displayed opportunity or assessment, never its color. */
@Composable
fun PortaOpportunityArchGlyph(
    semanticLabel: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(width = 26.dp, height = 32.dp).semantics { contentDescription = semanticLabel }) {
        val line = size.minDimension * 0.08f
        val arch = Path().apply {
            moveTo(line, size.height - line)
            lineTo(line, size.height * 0.42f)
            cubicTo(line, size.height * 0.12f, size.width * 0.25f, line, size.width / 2f, line)
            cubicTo(size.width * 0.75f, line, size.width - line, size.height * 0.12f, size.width - line, size.height * 0.42f)
            lineTo(size.width - line, size.height - line)
        }
        drawPath(arch, color, style = Stroke(width = line, cap = StrokeCap.Round))
        drawLine(color, Offset(line, size.height - line), Offset(size.width - line, size.height - line), line)
    }
}

/** Categorical result of one encoded requirement; this is not an admission result or progress percentage. */
enum class PortaThresholdRelation {
    CHECK_PASSED,
    CHECK_NOT_PASSED,
    UNKNOWN,
}

/** The marker moves only between categorical positions; it never encodes an invented distance to a requirement. */
@Composable
fun PortaThresholdGlyph(
    semanticLabel: String,
    relation: PortaThresholdRelation,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(width = 42.dp, height = 24.dp).semantics { contentDescription = semanticLabel }) {
        val line = size.minDimension * 0.085f
        val centerY = size.height / 2f
        val thresholdX = size.width * 0.53f
        drawLine(color, Offset(line, centerY), Offset(size.width - line, centerY), line, StrokeCap.Round)
        drawLine(color, Offset(thresholdX, line), Offset(thresholdX, size.height - line), line, StrokeCap.Round)
        val markerX = when (relation) {
            PortaThresholdRelation.CHECK_PASSED -> size.width * 0.78f
            PortaThresholdRelation.CHECK_NOT_PASSED -> size.width * 0.25f
            PortaThresholdRelation.UNKNOWN -> thresholdX
        }
        drawCircle(
            color = color,
            radius = size.minDimension * 0.16f,
            center = Offset(markerX, centerY),
            style = if (relation == PortaThresholdRelation.UNKNOWN) Stroke(width = line) else androidx.compose.ui.graphics.drawscope.Fill,
        )
    }
}

/** A single action-to-requirement link; callers decide which actual link exists. */
@Composable
fun PortaActionPathGlyph(
    semanticLabel: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(width = 42.dp, height = 20.dp).semantics { contentDescription = semanticLabel }) {
        val line = size.minDimension * 0.1f
        val mid = size.height / 2f
        val left = size.width * 0.16f
        val right = size.width * 0.84f
        drawLine(color, Offset(left, mid), Offset(right, mid), line, StrokeCap.Round)
        drawCircle(color, radius = size.minDimension * 0.16f, center = Offset(left, mid))
        drawCircle(color, radius = size.minDimension * 0.16f, center = Offset(right, mid), style = Stroke(width = line))
    }
}

/** Shape carries the state as well as color. The caller supplies the precise assessment label. */
enum class PortaStatusGlyphShape {
    CHECK,
    ACTION,
    VERIFY,
    TIMING_RISK,
    NEUTRAL,
}

@Composable
fun PortaStatusGlyph(
    semanticLabel: String,
    shape: PortaStatusGlyphShape,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(24.dp).semantics { contentDescription = semanticLabel }) {
        val line = size.minDimension * 0.09f
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.38f
        when (shape) {
            PortaStatusGlyphShape.CHECK -> {
                drawCircle(color, radius, center, style = Stroke(width = line))
                val check = Path().apply {
                    moveTo(size.width * 0.29f, size.height * 0.51f)
                    lineTo(size.width * 0.45f, size.height * 0.66f)
                    lineTo(size.width * 0.73f, size.height * 0.36f)
                }
                drawPath(check, color, style = Stroke(width = line, cap = StrokeCap.Round))
            }

            PortaStatusGlyphShape.ACTION -> {
                val diamond = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x + radius, center.y)
                    lineTo(center.x, center.y + radius)
                    lineTo(center.x - radius, center.y)
                    close()
                }
                drawPath(diamond, color, style = Stroke(width = line))
                drawCircle(color, radius = line * 0.8f, center = center)
            }

            PortaStatusGlyphShape.VERIFY -> {
                drawCircle(color, radius, center, style = Stroke(width = line))
                drawCircle(color, radius = line * 1.2f, center = center)
            }

            PortaStatusGlyphShape.TIMING_RISK -> {
                drawCircle(color, radius, center, style = Stroke(width = line))
                drawLine(color, center, Offset(center.x, center.y - radius * 0.62f), line, StrokeCap.Round)
                drawLine(color, center, Offset(center.x + radius * 0.48f, center.y), line, StrokeCap.Round)
            }

            PortaStatusGlyphShape.NEUTRAL -> drawCircle(color, radius, center, style = Stroke(width = line))
        }
    }
}

@Preview(name = "Editorial primitives · light", showBackground = true)
@Composable
private fun EditorialPrimitivesLightPreview() {
    AppTheme(isDarkMode = false) { EditorialPrimitivesPreviewContent() }
}

@Preview(name = "Editorial primitives · dark", showBackground = true)
@Composable
private fun EditorialPrimitivesDarkPreview() {
    AppTheme(isDarkMode = true) { EditorialPrimitivesPreviewContent() }
}

@Preview(name = "Editorial primitives · large text", showBackground = true, fontScale = 2f)
@Composable
private fun EditorialPrimitivesLargeTextPreview() {
    AppTheme(isDarkMode = false) { EditorialPrimitivesPreviewContent() }
}

@Composable
private fun EditorialPrimitivesPreviewContent() {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PortaOpportunityArchGlyph("Example opportunity", colors.primary)
            Text("Example opportunity", color = colors.text.primary)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PortaThresholdGlyph("IELTS requirement: current check needs action", PortaThresholdRelation.CHECK_NOT_PASSED, colors.status.warning)
            Text("IELTS check needs action", color = colors.text.primary)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PortaActionPathGlyph("Study action addresses the IELTS requirement", colors.primary)
            Text("Action → IELTS requirement", color = colors.text.primary)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PortaStatusGlyph("Source review required", PortaStatusGlyphShape.VERIFY, colors.status.warning)
            Text("Source review required", color = colors.text.primary)
        }
    }
}
