package io.github.vietdsk6.porta.presentation.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.components.bottomnav.BottomNavItem
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_home
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_home
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AdaptiveAppScaffoldUiTest {
    @Test
    fun `wide window uses navigation rail`() = runComposeUiTest {
        setContent {
            AppTheme {
                AdaptiveAppScaffold(
                    items = items,
                    selectedIndex = 0,
                    navigationVisible = true,
                    onClickItem = {},
                    modifier = Modifier.size(width = 1200.dp, height = 800.dp),
                ) {
                    Text("Workspace")
                }
            }
        }

        onNodeWithTag("porta-navigation-rail").assertExists()
    }

    @Test
    fun `compact window keeps bottom navigation`() = runComposeUiTest {
        setContent {
            AppTheme {
                AdaptiveAppScaffold(
                    items = items,
                    selectedIndex = 0,
                    navigationVisible = true,
                    onClickItem = {},
                    modifier = Modifier.size(width = 400.dp, height = 800.dp),
                ) {
                    Text("Workspace")
                }
            }
        }

        onNodeWithTag("porta-bottom-navigation").assertExists()
    }

    private val items = listOf(
        BottomNavItem(
            label = Res.string.bottom_nav_label_home,
            icon = UiRes.drawable.ic_home,
        ),
    )
}
