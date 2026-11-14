package io.github.vietdsk6.porta.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_logo
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.designsystem.util.PreviewHelper
import org.jetbrains.compose.resources.painterResource

@Composable
fun LogoImage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(UiRes.drawable.ic_logo),
            contentDescription = null,
            colorFilter = ColorFilter.tint(AppTheme.colors.primary),
            modifier =
            Modifier
                .size(180.dp)
                .align(Alignment.Center),
        )
    }
}

@Composable
@Preview
internal fun LogoImagePreview() {
    PreviewHelper {
        LogoImage()
    }
}
