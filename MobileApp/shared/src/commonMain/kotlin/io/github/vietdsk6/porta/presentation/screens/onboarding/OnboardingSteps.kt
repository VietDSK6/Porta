package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.onboarding_back_to_direction
import io.github.vietdsk6.porta.generated.resources.onboarding_demo_badge
import io.github.vietdsk6.porta.generated.resources.onboarding_demo_error_existing
import io.github.vietdsk6.porta.generated.resources.onboarding_demo_error_install
import io.github.vietdsk6.porta.generated.resources.onboarding_direction_eyebrow
import io.github.vietdsk6.porta.generated.resources.onboarding_direction_hint
import io.github.vietdsk6.porta.generated.resources.onboarding_direction_title
import io.github.vietdsk6.porta.generated.resources.onboarding_explore_unsaved
import io.github.vietdsk6.porta.generated.resources.onboarding_linh_route
import io.github.vietdsk6.porta.generated.resources.onboarding_local_privacy
import io.github.vietdsk6.porta.generated.resources.onboarding_route_explanation
import io.github.vietdsk6.porta.generated.resources.onboarding_route_eyebrow
import io.github.vietdsk6.porta.generated.resources.onboarding_route_intro
import io.github.vietdsk6.porta.generated.resources.onboarding_route_title
import io.github.vietdsk6.porta.generated.resources.onboarding_use_own_profile
import io.github.vietdsk6.porta.generated.resources.onboarding_verify_sources
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingDirectionStep() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(stringResource(Res.string.onboarding_direction_eyebrow), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.primary)
        Text(
            stringResource(Res.string.onboarding_direction_title),
            style = MaterialTheme.typography.displaySmall,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
    OnboardingPathMark()
    Text(
        stringResource(Res.string.onboarding_direction_hint),
        style = MaterialTheme.typography.titleMedium,
        color = AppTheme.colors.text.primary,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        stringResource(Res.string.onboarding_local_privacy),
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colors.text.secondary,
    )
}

@Composable
internal fun OnboardingRouteStep(
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.onboarding_route_eyebrow), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.primary)
        Text(stringResource(Res.string.onboarding_route_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(
            stringResource(Res.string.onboarding_route_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
    }
    uiState.thresholdLesson?.let { lesson ->
        OnboardingThresholdPreview(
            lesson = lesson,
            applied = uiState.isThresholdPreviewApplied,
            onToggle = { onUiEvent(OnBoardingUiEvent.ThresholdPreviewToggled) },
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(Res.string.onboarding_demo_badge), style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.primary)
        Text(stringResource(Res.string.onboarding_linh_route), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(stringResource(Res.string.onboarding_route_explanation), style = MaterialTheme.typography.bodyMedium)
    }
    OutlinedButton(
        onClick = { onUiEvent(OnBoardingUiEvent.BuildMyMap) },
        modifier = Modifier.fillMaxWidth().testTag("onboarding-build-profile"),
    ) { Text(stringResource(Res.string.onboarding_use_own_profile)) }
    TextButton(
        onClick = { onUiEvent(OnBoardingUiEvent.ExploreSample) },
        modifier = Modifier.fillMaxWidth().testTag("onboarding-explore-sample"),
    ) { Text(stringResource(Res.string.onboarding_explore_unsaved)) }
    TextButton(
        onClick = { onUiEvent(OnBoardingUiEvent.Back) },
        modifier = Modifier.fillMaxWidth().testTag("onboarding-back"),
    ) { Text(stringResource(Res.string.onboarding_back_to_direction)) }
    Text(
        stringResource(Res.string.onboarding_verify_sources),
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colors.text.secondary,
    )
    uiState.demoProfileError?.let { error ->
        Text(
            stringResource(
                when (error) {
                    OnboardingDemoError.EXISTING_PROFILE -> Res.string.onboarding_demo_error_existing
                    OnboardingDemoError.INSTALL_FAILED -> Res.string.onboarding_demo_error_install
                },
            ),
            color = AppTheme.colors.status.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
