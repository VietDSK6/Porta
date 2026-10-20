package io.github.vietdsk6.porta.presentation.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.SubscriptionRepository
import io.github.vietdsk6.porta.data.repository.UserRepository
import io.github.vietdsk6.porta.designsystem.components.SettingsItemUiState
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_settings_item_logout
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_settings_item_subscriptions
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_settings_item_support_legal
import io.github.vietdsk6.porta.domain.model.isFree
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.help_and_support
import io.github.vietdsk6.porta.generated.resources.logout
import io.github.vietdsk6.porta.generated.resources.subscriptions
import io.github.vietdsk6.porta.root.AppConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val userRepository: UserRepository,
    subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val subscriptionsItem = SettingsItemUiState(
        startIcon = UiRes.drawable.ic_settings_item_subscriptions,
        textRes = Res.string.subscriptions,
    )
    private val supportItem = SettingsItemUiState(
        startIcon = UiRes.drawable.ic_settings_item_support_legal,
        textRes = Res.string.help_and_support,
    )
    private val logoutItem = SettingsItemUiState(
        startIcon = UiRes.drawable.ic_settings_item_logout,
        textRes = Res.string.logout,
        showEndIcon = false,
    )

    private fun settingsItemsFor(isSignedIn: Boolean): List<SettingsItemUiState> = buildList {
        // Subscriptions row only when premium features are enabled.
        if (AppConfiguration.PREMIUM_FEATURES_ENABLED) add(subscriptionsItem)
        add(supportItem)
        if (AppConfiguration.AUTH_SOCIAL_LOGIN_ENABLED && isSignedIn) add(logoutItem)
    }

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> =
        combine(
            userRepository.currentUser,
            subscriptionRepository.currentSubscriptionFlow,
            _uiState,
        ) { currentUser, currentSubscription, uiState ->
            val user = currentUser.getOrNull()
            uiState.copy(
                user = if (user?.isAnonymous == true && AppConfiguration.AUTH_SOCIAL_LOGIN_ENABLED) null else user,
                settingsItemList = settingsItemsFor(isSignedIn = user != null),
                showUpgradePremiumBanner = AppConfiguration.PREMIUM_FEATURES_ENABLED && currentSubscription.isFree,
            )
        }.stateIn(viewModelScope, WhileSubscribed(5000), _uiState.value)

    fun onUiEvent(event: AccountUiEvent) = viewModelScope.launch {
        when (event) {
            AccountUiEvent.OnLogoutConfirmClick -> {
                userRepository.logOut()
                _uiState.update { it.copy(isLogoutDialogVisible = false) }
            }

            AccountUiEvent.OnLogoutDialogDismiss -> {
                _uiState.update { it.copy(isLogoutDialogVisible = false) }
            }

            is AccountUiEvent.OnSettingsItemClick -> {
                when (event.item.textRes) {
                    Res.string.logout -> {
                        _uiState.update { it.copy(isLogoutDialogVisible = true) }
                    }

                    else -> {}
                }
            }

            AccountUiEvent.OnClickUpgradePremium -> Unit

            AccountUiEvent.OnClickProfile -> Unit

            AccountUiEvent.OnClickSignIn -> Unit
        }
    }
}
