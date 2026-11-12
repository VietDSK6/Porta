package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlanActions(
    plan: ActivePlanCardUiState,
    isWorking: Boolean,
    onUiEvent: (ActivePlanUiEvent) -> Unit,
    onPreviewImpact: (String) -> Unit,
    onUpdateProfile: () -> Unit,
) {
    var showRemoveConfirmation by remember(plan.id) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (plan.status) {
            ActivePlanStatusUi.PLANNED -> {
                Button(
                    modifier = Modifier.fillMaxWidth().testTag("start-plan-button"),
                    enabled = !isWorking,
                    onClick = { onUiEvent(ActivePlanUiEvent.Start) },
                ) {
                    Text(stringResource(Res.string.active_start))
                }
            }

            ActivePlanStatusUi.IN_PROGRESS -> {
                Button(
                    modifier = Modifier.fillMaxWidth().testTag("complete-plan-button"),
                    enabled = !isWorking,
                    onClick = { onUiEvent(ActivePlanUiEvent.Complete) },
                ) {
                    Text(stringResource(Res.string.active_complete))
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isWorking,
                    onClick = { onUiEvent(ActivePlanUiEvent.Pause) },
                ) {
                    Text(stringResource(Res.string.active_pause))
                }
            }

            ActivePlanStatusUi.PAUSED -> {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isWorking,
                    onClick = { onUiEvent(ActivePlanUiEvent.Resume) },
                ) {
                    Text(stringResource(Res.string.active_resume))
                }
            }

            ActivePlanStatusUi.COMPLETED -> {
                Button(
                    modifier = Modifier.fillMaxWidth().testTag("update-profile-button"),
                    enabled = !isWorking,
                    onClick = onUpdateProfile,
                ) {
                    Text(stringResource(Res.string.active_confirm_result))
                }
            }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onPreviewImpact(plan.actionId) },
        ) {
            Text(stringResource(Res.string.active_preview_again))
        }
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isWorking,
            onClick = { showRemoveConfirmation = true },
        ) {
            Text(stringResource(Res.string.active_remove_local), color = AppTheme.colors.status.error)
        }
    }
    if (showRemoveConfirmation) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = { Text(stringResource(Res.string.active_remove_title)) },
            text = { Text(stringResource(Res.string.active_remove_help)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveConfirmation = false
                        onUiEvent(ActivePlanUiEvent.Remove)
                    },
                ) { Text(stringResource(Res.string.active_remove_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmation = false }) { Text(stringResource(Res.string.active_remove_cancel)) }
            },
        )
    }
}
