package io.github.vietdsk6.porta.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.components.bottomnav.BottomNavItem
import io.github.vietdsk6.porta.designsystem.components.bottomnav.BottomNavigationBar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Keeps Porta's information architecture identical across devices while adapting its chrome:
 * compact windows use thumb-friendly bottom navigation and wide windows use a persistent rail.
 */
@Composable
internal fun AdaptiveAppScaffold(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    navigationVisible: Boolean,
    onClickItem: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= NAVIGATION_RAIL_BREAKPOINT) {
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = navigationVisible,
                    enter = fadeIn(snap()),
                    exit = fadeOut(snap()),
                ) {
                    PortaNavigationRail(
                        items = items,
                        selectedIndex = selectedIndex,
                        onClickItem = onClickItem,
                    )
                }
                Box(modifier = Modifier.fillMaxHeight().weight(1f)) { content() }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) { content() }
                AnimatedVisibility(
                    visible = navigationVisible,
                    enter = fadeIn(snap()),
                    exit = fadeOut(snap()),
                ) {
                    BottomNavigationBar(
                        modifier = Modifier.fillMaxWidth().testTag("porta-bottom-navigation"),
                        items = items,
                        selectedIndex = selectedIndex,
                        onClickItem = onClickItem,
                    )
                }
            }
        }
    }
}

@Composable
private fun PortaNavigationRail(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onClickItem: (BottomNavItem) -> Unit,
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight().testTag("porta-navigation-rail"),
        containerColor = AppTheme.colors.bottomNav.background,
        header = {
            Text(
                text = "PORTA",
                modifier = Modifier.padding(vertical = 20.dp),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
            )
        },
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            val icon = if (isSelected) item.selectedIcon ?: item.icon else item.icon
            NavigationRailItem(
                selected = isSelected,
                enabled = item.icon != null || item.selectedIcon != null,
                alwaysShowLabel = true,
                onClick = { onClickItem(item) },
                icon = {
                    icon?.let {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(it),
                            contentDescription = stringResource(item.label),
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        style = AppTheme.typography.bodyExtraSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = AppTheme.colors.bottomNav.selectedTextIcon,
                    selectedTextColor = AppTheme.colors.bottomNav.selectedTextIcon,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = AppTheme.colors.bottomNav.unselectedTextIcon,
                    unselectedTextColor = AppTheme.colors.bottomNav.unselectedTextIcon,
                ),
            )
        }
    }
}

private val NAVIGATION_RAIL_BREAKPOINT = 840.dp
