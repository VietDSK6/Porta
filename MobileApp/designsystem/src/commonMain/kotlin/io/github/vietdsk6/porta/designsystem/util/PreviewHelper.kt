package io.github.vietdsk6.porta.designsystem.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.designsystem.theme.LocalThemeIsDark

@Composable
fun PreviewHelper(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    AppTheme(isDarkMode = LocalThemeIsDark.current) {
        FlowRow(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.horizontalItemSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.verticalListItemSpacing),
            content = { content() },
        )
    }
}
