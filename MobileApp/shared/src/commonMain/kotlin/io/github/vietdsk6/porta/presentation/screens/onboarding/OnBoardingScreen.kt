package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.LogoImage
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.locale.LocalAppLocale

@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnBoardingViewModel,
    onOnBoardingFinished: (OnboardingCompletion) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.completion) {
        uiState.completion?.let { completion ->
            onOnBoardingFinished(completion)
            viewModel.onFinishHandled()
        }
    }

    OnBoardingScreen(
        modifier = modifier,
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
    )
}

/**
 * Pure overload — no ViewModel, so it renders from plain state in
 * `runComposeUiTest` and `@Preview`. Navigation side effects stay in the
 * ViewModel overload above.
 */
@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(AppTheme.colors.background)) {
        if (uiState.isLoading) {
            SplashLogo(modifier = Modifier.fillMaxSize())
        } else {
            OnboardingFlow(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onUiEvent = onUiEvent,
            )
        }
    }
}

@Composable
private fun SplashLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(AppTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        LogoImage()
    }
}

// Snapshotted by PreviewScreenshotTest — `./gradlew :shared:recordRoborazziAndroidHostTest`
// renders these to shared/src/androidHostTest/snapshots/. See the `verify-ui` skill.
// `isLoading = false` skips the splash so the actual onboarding content renders.

@Preview
@Composable
private fun OnBoardingScreenPreview() {
    AppTheme {
        OnBoardingScreen(
            uiState = OnBoardingUiState(
                selectedGoal = OnboardingGoal.SCHOLARSHIPS,
                isLoading = false,
            ),
            onUiEvent = {},
        )
    }
}

@Preview
@Composable
private fun OnBoardingRoutePreview() {
    AppTheme {
        OnBoardingScreen(
            uiState = OnBoardingUiState(
                currentStep = 1,
                selectedGoal = OnboardingGoal.SCHOLARSHIPS,
                isLoading = false,
            ),
            onUiEvent = {},
        )
    }
}

@Preview(name = "Onboarding route Vietnamese", locale = "vi")
@Composable
private fun OnBoardingRouteVietnamesePreview() {
    CompositionLocalProvider(LocalAppLocale provides "vi") {
        AppTheme {
            OnBoardingScreen(
                uiState = OnBoardingUiState(
                    currentStep = 1,
                    selectedGoal = OnboardingGoal.SCHOLARSHIPS,
                    isLoading = false,
                ),
                onUiEvent = {},
            )
        }
    }
}
