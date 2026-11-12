package io.github.vietdsk6.porta.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.domain.model.CostBand

/** Shared layout thresholds for the compact timeline and wide planning workspace. */
object PortaBreakpoints {
    val wide = 840.dp
    val workbench = 900.dp
    const val maxWideFontScale = 1.5f
}

object PortaDimens {
    val compactScreenInset = 16.dp
    val wideScreenInset = 28.dp
    val sectionGap = 20.dp
    val cardCorner = 20.dp
}

/** Planning cost colors are stable across Route and the Action Frontier. */
object PortaColors {
    val costFree = Color(0xFF0F9D8A)
    val costLow = Color(0xFF3976D9)
    val costMedium = Color(0xFFD18A00)
    val costHigh = Color(0xFFC94B56)
    val costUnknown = Color(0xFF74717D)

    fun cost(band: CostBand): Color = when (band) {
        CostBand.FREE -> costFree
        CostBand.LOW -> costLow
        CostBand.MEDIUM -> costMedium
        CostBand.HIGH -> costHigh
        CostBand.UNKNOWN -> costUnknown
    }
}
