package io.github.vietdsk6.porta.presentation.screens.subscriptions

import io.github.vietdsk6.porta.domain.model.Subscription

data class SubscriptionsUiState(
    val isLoading: Boolean = false,
    val showUpgradePremiumBanner: Boolean = true,
    val currentPlan: Subscription? = null,
    val isMock: Boolean = false,
)

sealed interface SubscriptionsUiEvent {
    data object OnCancelMockSubscription : SubscriptionsUiEvent
}
