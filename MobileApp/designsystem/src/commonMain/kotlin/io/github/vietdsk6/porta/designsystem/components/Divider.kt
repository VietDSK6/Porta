package io.github.vietdsk6.porta.designsystem.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.designsystem.util.PreviewHelper

@Composable
fun Divider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = AppTheme.colors.outline,
) {
    HorizontalDivider(
        color = color,
        thickness = thickness,
        modifier = modifier,
    )
}

@Preview
@Composable
internal fun DividerPreview() {
    PreviewHelper {
        Divider()
    }
}
