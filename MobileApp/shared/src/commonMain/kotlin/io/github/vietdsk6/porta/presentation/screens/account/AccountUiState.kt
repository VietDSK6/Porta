package io.github.vietdsk6.porta.presentation.screens.account

import io.github.vietdsk6.porta.designsystem.components.SettingsItemUiState
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_settings_item_logout
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_settings_item_subscriptions
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_settings_item_support_legal
import io.github.vietdsk6.porta.domain.model.User
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.help_and_support
import io.github.vietdsk6.porta.generated.resources.logout
import io.github.vietdsk6.porta.generated.resources.subscriptions

data class AccountUiState(
    val settingsItemList: List<SettingsItemUiState> = listOf(
        SettingsItemUiState(
            startIcon = UiRes.drawable.ic_settings_item_subscriptions,
            textRes = Res.string.subscriptions,
        ),

        SettingsItemUiState(
            startIcon = UiRes.drawable.ic_settings_item_support_legal,
            textRes = Res.string.help_and_support,
        ),

        SettingsItemUiState(
            startIcon = UiRes.drawable.ic_settings_item_logout,
            textRes = Res.string.logout,
            showEndIcon = false,
        ),
    ),
    val user: User? = null,
    val isLogoutDialogVisible: Boolean = false,
    val showUpgradePremiumBanner: Boolean = false,
)

sealed interface AccountUiEvent {
    data class OnSettingsItemClick(val item: SettingsItemUiState) : AccountUiEvent
    data object OnLogoutConfirmClick : AccountUiEvent
    data object OnLogoutDialogDismiss : AccountUiEvent
    data object OnClickUpgradePremium : AccountUiEvent
    data object OnClickSignIn : AccountUiEvent
    data object OnClickProfile : AccountUiEvent
}
