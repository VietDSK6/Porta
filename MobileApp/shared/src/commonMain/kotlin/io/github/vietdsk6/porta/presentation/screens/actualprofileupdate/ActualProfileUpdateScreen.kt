package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

@Composable
fun ActualProfileUpdateScreen(
    viewModel: ActualProfileUpdateViewModel,
    onNavigateBack: () -> Unit,
    onProfileUpdated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.onUiEvent(ActualProfileUpdateUiEvent.SaveHandled)
            onProfileUpdated()
        }
    }

    ActualProfileUpdateScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun ActualProfileUpdateScreen(
    uiState: ActualProfileUpdateUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("actual-profile-update-screen"),
        isScrollableContent = true,
        title = "Update actual profile",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        when {
            uiState.isLoading -> LoadingState()
            !uiState.hasCompletedPlan -> UnavailableState(onNavigateBack)
            else -> UpdateForm(uiState, onUiEvent, onNavigateBack)
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            "Reading your local plan…",
            modifier = Modifier.padding(top = 12.dp),
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
private fun UnavailableState(onNavigateBack: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("No completed milestone to review", style = AppTheme.typography.h5)
            Text(
                "Mark the work complete in your local plan first. " +
                    "Porta will keep your actual facts unchanged until this confirmation step.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyLarge,
            )
            Button(modifier = Modifier.fillMaxWidth(), onClick = onNavigateBack) {
                Text("Return to active plan")
            }
        }
    }
}

@Composable
private fun UpdateForm(
    uiState: ActualProfileUpdateUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
        BoundaryBanner()
        ValueComparisonCard(uiState)
        AchievedValueCard(uiState, onUiEvent)
        EvidenceCard(uiState, onUiEvent)
        ConfirmationCard()
        uiState.error?.let { error ->
            Text(
                error,
                modifier = Modifier.testTag("actual-profile-update-error"),
                color = AppTheme.colors.status.error,
                style = AppTheme.typography.bodyMedium,
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth().testTag("confirm-actual-profile-update"),
            enabled = uiState.canConfirm,
            onClick = { onUiEvent(ActualProfileUpdateUiEvent.Confirm) },
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = AppTheme.colors.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Confirm actual profile update")
            }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().testTag("cancel-actual-profile-update"),
            onClick = onNavigateBack,
        ) {
            Text("Not yet — keep profile unchanged")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun BoundaryBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "PLAN COMPLETE · PROFILE STILL UNCHANGED",
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                "Confirm what is true now. The achieved value—not the plan target—will become your actual fact.",
                color = AppTheme.colors.onAlternative,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ValueComparisonCard(uiState: ActualProfileUpdateUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(uiState.factLabel, style = AppTheme.typography.h5)
            Text(uiState.planTitle, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ValueTile(
                    eyebrow = "CURRENT ACTUAL",
                    value = uiState.currentValueLabel,
                    modifier = Modifier.weight(1f),
                )
                ValueTile(
                    eyebrow = "PLANNED TARGET",
                    value = uiState.plannedTargetLabel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ValueTile(
    eyebrow: String,
    value: String,
    modifier: Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                eyebrow,
                color = AppTheme.colors.text.secondary,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall,
            )
            Text(value, color = AppTheme.colors.text.primary, style = AppTheme.typography.h6)
        }
    }
}

@Composable
private fun AchievedValueCard(
    uiState: ActualProfileUpdateUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
) {
    FormCard(
        title = "What did you actually achieve?",
        description = "The planned target is prefilled. Change it if your real result was different.",
    ) {
        when (uiState.inputKind) {
            ActualValueInputKind.BOOLEAN -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    modifier = Modifier.testTag("achieved-value-yes"),
                    selected = uiState.achievedBoolean == true,
                    onClick = { onUiEvent(ActualProfileUpdateUiEvent.AchievedBooleanChanged(true)) },
                    label = { Text("Yes") },
                )
                FilterChip(
                    modifier = Modifier.testTag("achieved-value-no"),
                    selected = uiState.achievedBoolean == false,
                    onClick = { onUiEvent(ActualProfileUpdateUiEvent.AchievedBooleanChanged(false)) },
                    label = { Text("No") },
                )
            }

            ActualValueInputKind.NUMBER,
            ActualValueInputKind.TEXT,
            -> OutlinedTextField(
                modifier = Modifier.fillMaxWidth().testTag("achieved-value-input"),
                value = uiState.achievedValue,
                onValueChange = { onUiEvent(ActualProfileUpdateUiEvent.AchievedValueChanged(it)) },
                label = { Text("Achieved value") },
                suffix = uiState.unitLabel.takeIf(String::isNotBlank)?.let { unit ->
                    { Text(unit) }
                },
                supportingText = uiState.achievedValueError?.let { message ->
                    { Text(message) }
                },
                isError = uiState.achievedValueError != null,
                singleLine = true,
            )
        }
    }
}

@Composable
private fun EvidenceCard(
    uiState: ActualProfileUpdateUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
) {
    FormCard(
        title = "Evidence (optional)",
        description = "Store a local note about what supports this fact. Porta does not upload the file.",
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                modifier = Modifier.testTag("include-evidence-checkbox"),
                checked = uiState.includeEvidence,
                onCheckedChange = { onUiEvent(ActualProfileUpdateUiEvent.IncludeEvidenceChanged(it)) },
            )
            Text("Add evidence details", style = AppTheme.typography.bodyLarge)
        }
        if (uiState.includeEvidence) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().testTag("evidence-label-input"),
                value = uiState.evidenceLabel,
                onValueChange = { onUiEvent(ActualProfileUpdateUiEvent.EvidenceLabelChanged(it)) },
                label = { Text("Evidence label") },
                placeholder = { Text("IELTS result, transcript, certificate…") },
                supportingText = if (uiState.evidenceLabel.isBlank()) {
                    { Text("Give the evidence a short label") }
                } else {
                    null
                },
                isError = uiState.evidenceLabel.isBlank(),
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().testTag("evidence-reference-input"),
                value = uiState.evidenceReference,
                onValueChange = { onUiEvent(ActualProfileUpdateUiEvent.EvidenceReferenceChanged(it)) },
                label = { Text("Reference or local note") },
                placeholder = { Text("Optional file name, URL, or note") },
                singleLine = true,
            )
        }
    }
}

@Composable
private fun ConfirmationCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE9FBF7)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(
            "By confirming, you assert that this achieved value is true now. " +
                "Doors will immediately re-evaluate against it; eligibility still does not mean selection.",
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = Color(0xFF0B4C43),
            style = AppTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun FormCard(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = AppTheme.typography.h6)
                Text(description, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
            }
            content()
        }
    }
}

@Preview
@Composable
private fun ActualProfileUpdateScreenPreview() {
    AppTheme {
        ActualProfileUpdateScreen(
            uiState = ActualProfileUpdateUiState(
                isLoading = false,
                hasCompletedPlan = true,
                planTitle = "Raise IELTS to 7.0 band",
                factLabel = "IELTS score",
                currentValueLabel = "6.5 band",
                plannedTargetLabel = "7.0 band",
                achievedValue = "7.0",
                unitLabel = "band",
            ),
            onUiEvent = {},
            onNavigateBack = {},
        )
    }
}
