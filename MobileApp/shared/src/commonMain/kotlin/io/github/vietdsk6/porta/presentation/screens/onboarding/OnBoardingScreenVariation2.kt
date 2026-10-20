package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The starter kit keeps two style hooks for downstream apps. Porta intentionally uses one
 * product-specific onboarding flow for both so the demo semantics cannot drift by theme choice.
 */
@Composable
fun OnBoardingScreenVariation2(
    modifier: Modifier = Modifier,
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    OnBoardingScreenVariation1(
        modifier = modifier,
        uiState = uiState,
        onUiEvent = onUiEvent,
    )
}
