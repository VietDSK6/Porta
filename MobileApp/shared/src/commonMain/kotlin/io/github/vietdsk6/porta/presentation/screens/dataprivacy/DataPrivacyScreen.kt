package io.github.vietdsk6.porta.presentation.screens.dataprivacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileDataAction
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiEvent
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileViewModel
import io.github.vietdsk6.porta.presentation.screens.studentprofile.confirmationBody
import io.github.vietdsk6.porta.presentation.screens.studentprofile.confirmationButton
import io.github.vietdsk6.porta.presentation.screens.studentprofile.confirmationTitle

@Composable
fun DataPrivacyScreen(
    viewModel: StudentProfileViewModel,
    onNavigateBack: () -> Unit,
    onOpenPackInspector: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DataPrivacyScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onOpenPackInspector = onOpenPackInspector,
        modifier = modifier,
    )
}

@Composable
fun DataPrivacyScreen(
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenPackInspector: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.fillMaxSize().testTag("data-privacy-screen"),
        isScrollableContent = true,
        title = "Data & privacy",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Your profile and plans are stored on this device. You can inspect the rules, export a copy of your profile metadata, or remove local data here.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )

            DataSection(title = "Review and export") {
                DataActionRow(
                    title = "Opportunity pack",
                    description = "See which encoded checks use your profile facts.",
                    action = "Inspect",
                    tag = "student-profile-pack-inspector",
                    enabled = !uiState.isWorking && !uiState.isLoading,
                    onClick = onOpenPackInspector,
                )
                HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.24f))
                DataActionRow(
                    title = "Profile metadata",
                    description = "Export facts, programme confirmations and evidence references as JSON. Evidence files are not included.",
                    action = "Export",
                    tag = "student-profile-export",
                    enabled = uiState.hasRealProfile && !uiState.isWorking && !uiState.isLoading,
                    onClick = { onUiEvent(StudentProfileUiEvent.ExportMetadata) },
                )
            }

            DataSection(title = "Remove local data") {
                DataActionRow(
                    title = "Academic profile and plans",
                    description = "Remove facts, programme confirmations and plans. Keep saved doors, catalog and preferences.",
                    action = "Clear",
                    tag = "student-profile-clear",
                    enabled = uiState.hasRealProfile && !uiState.isWorking && !uiState.isLoading,
                    actionColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        onUiEvent(StudentProfileUiEvent.DataActionRequested(ProfileDataAction.CLEAR_PROFILE_AND_PLANS))
                    },
                )
                HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.24f))
                DataActionRow(
                    title = "All Porta data on this device",
                    description = "Remove profile, plans, saved doors, imported catalogs and preferences. The bundled catalog is restored.",
                    action = "Reset",
                    tag = "student-profile-reset-all",
                    enabled = !uiState.isWorking && !uiState.isLoading,
                    actionColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        onUiEvent(StudentProfileUiEvent.DataActionRequested(ProfileDataAction.RESET_ALL_LOCAL_DATA))
                    },
                )
            }

            uiState.message?.let { message ->
                Surface(color = AppTheme.colors.alternative, shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.weight(1f),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.text.primary,
                        )
                        TextButton(onClick = { onUiEvent(StudentProfileUiEvent.MessageHandled) }) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }

    uiState.pendingDataAction?.let { action ->
        AlertDialog(
            modifier = Modifier.testTag("student-profile-data-confirmation"),
            onDismissRequest = { onUiEvent(StudentProfileUiEvent.DataActionCancelled) },
            title = { Text(action.confirmationTitle()) },
            text = { Text(action.confirmationBody()) },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("student-profile-data-confirm"),
                    onClick = { onUiEvent(StudentProfileUiEvent.DataActionConfirmed) },
                ) {
                    Text(action.confirmationButton())
                }
            },
            dismissButton = {
                TextButton(onClick = { onUiEvent(StudentProfileUiEvent.DataActionCancelled) }) {
                    Text("Keep data")
                }
            },
        )
    }
}

@Composable
private fun DataSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = AppTheme.typography.h5,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.surfaceContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun DataActionRow(
    title: String,
    description: String,
    action: String,
    tag: String,
    enabled: Boolean,
    onClick: () -> Unit,
    actionColor: Color = AppTheme.colors.primary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.primary.copy(alpha = if (enabled) 1f else 0.5f),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary.copy(alpha = if (enabled) 1f else 0.5f),
            )
        }
        Text(
            text = action,
            style = AppTheme.typography.bodyMedium,
            color = actionColor.copy(alpha = if (enabled) 1f else 0.5f),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview
@Composable
private fun DataPrivacyScreenPreview() {
    AppTheme {
        DataPrivacyScreen(
            uiState = StudentProfileUiState(isLoading = false, hasRealProfile = true),
            onUiEvent = {},
            onNavigateBack = {},
            onOpenPackInspector = {},
        )
    }
}
