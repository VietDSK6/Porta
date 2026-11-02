package io.github.vietdsk6.porta.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import io.github.vietdsk6.porta.presentation.components.porta.PortaActionPathGlyph
import io.github.vietdsk6.porta.presentation.components.porta.PortaOpportunityArchGlyph
import io.github.vietdsk6.porta.presentation.components.porta.PortaStatusGlyph
import io.github.vietdsk6.porta.presentation.components.porta.PortaStatusGlyphShape
import io.github.vietdsk6.porta.presentation.components.porta.PortaThresholdGlyph
import io.github.vietdsk6.porta.presentation.components.porta.PortaThresholdRelation
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EditorialVisualPrimitivesUiTest {
    @Test
    fun `each primitive exposes the assessment supplied label`() = runComposeUiTest {
        setContent {
            Column {
                PortaOpportunityArchGlyph("Example grant, source review needed", Color.Black)
                PortaThresholdGlyph(
                    "IELTS check: current 6.5, required 7.0, needs action",
                    PortaThresholdRelation.CHECK_NOT_PASSED,
                    Color.Black,
                )
                PortaActionPathGlyph("IELTS study action addresses one checked requirement", Color.Black)
                PortaStatusGlyph("Deadline needs review", PortaStatusGlyphShape.TIMING_RISK, Color.Black)
            }
        }

        onNodeWithContentDescription("Example grant, source review needed").assertExists()
        onNodeWithContentDescription("IELTS check: current 6.5, required 7.0, needs action").assertExists()
        onNodeWithContentDescription("IELTS study action addresses one checked requirement").assertExists()
        onNodeWithContentDescription("Deadline needs review").assertExists()
    }
}
