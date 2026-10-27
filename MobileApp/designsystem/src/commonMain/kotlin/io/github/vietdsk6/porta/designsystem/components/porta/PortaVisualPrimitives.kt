package io.github.vietdsk6.porta.designsystem.components.porta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Porta's threshold shape. The shape is visual only: callers own every status label and semantic.
 */
@Composable
fun PortaArch(
    modifier: Modifier = Modifier,
    containerColor: Color,
    outlineColor: Color,
    borderWidth: Dp = 1.dp,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .clip(PortaArchShape)
            .border(BorderStroke(borderWidth, outlineColor), PortaArchShape),
        contentAlignment = contentAlignment,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(containerColor)
        }
        content()
    }
}

enum class PortaConnectorPosition {
    SINGLE,
    FIRST,
    MIDDLE,
    LAST,
}

/** Decorative path segment used to connect one source to one or more computed targets. */
@Composable
fun PortaConnectorPath(
    position: PortaConnectorPosition,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp,
) {
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        val junctionX = size.width * 0.45f
        val junctionY = size.height * 0.5f
        val verticalStart = if (position == PortaConnectorPosition.SINGLE) {
            0f
        } else {
            if (position == PortaConnectorPosition.FIRST) 0f else -strokePx
        }
        val verticalEnd = when (position) {
            PortaConnectorPosition.SINGLE,
            PortaConnectorPosition.LAST,
            -> junctionY

            PortaConnectorPosition.FIRST,
            PortaConnectorPosition.MIDDLE,
            -> size.height + strokePx
        }
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(junctionX, verticalStart),
            end = androidx.compose.ui.geometry.Offset(junctionX, verticalEnd),
            strokeWidth = strokePx,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(junctionX, junctionY),
            end = androidx.compose.ui.geometry.Offset(size.width, junctionY),
            strokeWidth = strokePx,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = color,
            radius = strokePx * 1.65f,
            center = androidx.compose.ui.geometry.Offset(junctionX, junctionY),
            style = Stroke(width = strokePx),
        )
    }
}

private val PortaArchShape = GenericShape { size, _ ->
    val crownHeight = minOf(size.width * 0.5f, size.height * 0.48f)
    moveTo(0f, size.height)
    lineTo(0f, crownHeight)
    cubicTo(
        0f,
        crownHeight * 0.45f,
        size.width * 0.22f,
        0f,
        size.width * 0.5f,
        0f,
    )
    cubicTo(
        size.width * 0.78f,
        0f,
        size.width,
        crownHeight * 0.45f,
        size.width,
        crownHeight,
    )
    lineTo(size.width, size.height)
    close()
}
