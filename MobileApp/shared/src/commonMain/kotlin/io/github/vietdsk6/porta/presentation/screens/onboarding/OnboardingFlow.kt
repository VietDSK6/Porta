package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.onboarding_brand
import io.github.vietdsk6.porta.generated.resources.onboarding_continue
import io.github.vietdsk6.porta.generated.resources.onboarding_explore_sample
import io.github.vietdsk6.porta.generated.resources.onboarding_opening_route
import io.github.vietdsk6.porta.generated.resources.onboarding_show_linh_route
import io.github.vietdsk6.porta.generated.resources.onboarding_step_count
import io.github.vietdsk6.porta.presentation.theme.PortaDimens
import org.jetbrains.compose.resources.stringResource

private const val TOTAL_STEPS = 2

@Composable
fun OnboardingFlow(
    modifier: Modifier = Modifier,
    uiState: OnBoardingUiState,
    onUiEvent: (OnBoardingUiEvent) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingHeader(uiState.currentStep, onSkip = { onUiEvent(OnBoardingUiEvent.SkipDemo) })
        Column(
            modifier = Modifier.weight(1f).widthIn(max = 620.dp).fillMaxWidth()
                .verticalScroll(remember(uiState.currentStep) { ScrollState(0) })
                .padding(horizontal = PortaDimens.compactScreenInset, vertical = PortaDimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(PortaDimens.sectionGap),
        ) {
            if (uiState.currentStep == 0) {
                OnboardingDirectionStep()
            } else {
                OnboardingRouteStep(uiState, onUiEvent)
            }
        }
        if (uiState.currentStep == 0) {
            Button(
                onClick = { onUiEvent(OnBoardingUiEvent.Next) },
                modifier = Modifier.widthIn(max = 588.dp).fillMaxWidth()
                    .padding(horizontal = PortaDimens.compactScreenInset, vertical = 12.dp)
                    .testTag("onboarding-continue"),
            ) { Text(stringResource(Res.string.onboarding_continue)) }
        } else {
            Button(
                onClick = { onUiEvent(OnBoardingUiEvent.UseDemoProfile) },
                enabled = !uiState.isInstallingDemoProfile,
                modifier = Modifier.widthIn(max = 588.dp).fillMaxWidth()
                    .padding(horizontal = PortaDimens.compactScreenInset, vertical = 12.dp)
                    .testTag("onboarding-use-demo-profile"),
            ) {
                Text(
                    stringResource(
                        if (uiState.isInstallingDemoProfile) {
                            Res.string.onboarding_opening_route
                        } else {
                            Res.string.onboarding_show_linh_route
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun OnboardingHeader(currentStep: Int, onSkip: () -> Unit) {
    val stepDescription = stringResource(Res.string.onboarding_step_count, currentStep + 1)
    Column(
        modifier = Modifier.widthIn(max = 620.dp).fillMaxWidth().testTag("onboarding-header")
            .padding(horizontal = PortaDimens.compactScreenInset, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.onboarding_brand), style = MaterialTheme.typography.titleMedium, color = AppTheme.colors.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onSkip, modifier = Modifier.testTag("onboarding-skip")) {
                Text(stringResource(Res.string.onboarding_explore_sample))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().testTag("onboarding-progress")
                .semantics { stateDescription = stepDescription },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(TOTAL_STEPS) { index ->
                Box(
                    Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(100))
                        .background(if (index <= currentStep) AppTheme.colors.primary else AppTheme.colors.outline),
                )
            }
        }
    }
}
