package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vinceglb.filekit.mimeType
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EvidenceCard(
    uiState: ActualProfileUpdateUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
    onPickEvidenceFile: () -> Unit,
) {
    val description = when {
        uiState.isEvidenceRequired &&
            uiState.inputKind == ActualValueInputKind.BOOLEAN &&
            uiState.achievedBoolean == null ->
            stringResource(Res.string.actual_evidence_select_first)

        uiState.isEvidenceRequired &&
            uiState.inputKind == ActualValueInputKind.BOOLEAN &&
            uiState.achievedBoolean == false ->
            stringResource(Res.string.actual_evidence_no_need)

        uiState.requiresSupportingEvidence ->
            stringResource(Res.string.actual_evidence_required_help)

        else -> stringResource(Res.string.actual_evidence_optional_help)
    }
    val attachmentError = uiState.localizedEvidenceError()
    FormCard(
        title = stringResource(Res.string.actual_evidence_title),
        description = description,
    ) {
        if (uiState.requiresSupportingEvidence) {
            Text(
                text = stringResource(Res.string.actual_evidence_required_yes),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        } else if (!uiState.isEvidenceRequired) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    modifier = Modifier.testTag("include-evidence-checkbox"),
                    checked = uiState.includeEvidence,
                    onCheckedChange = { onUiEvent(ActualProfileUpdateUiEvent.IncludeEvidenceChanged(it)) },
                )
                Text(stringResource(Res.string.actual_evidence_details), style = AppTheme.typography.bodyLarge)
            }
        }
        if (uiState.showEvidenceInputs) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth().testTag("pick-evidence-attachment"),
                enabled = !uiState.isSaving,
                onClick = onPickEvidenceFile,
            ) {
                Text(
                    when {
                        uiState.evidenceAttachment != null -> stringResource(Res.string.actual_replace_file)
                        uiState.requiresSupportingEvidence -> stringResource(Res.string.actual_attach_recommended)
                        else -> stringResource(Res.string.actual_attach_file)
                    },
                )
            }
            Text(
                stringResource(Res.string.actual_accepted_files),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            uiState.evidenceAttachment?.let { attachment ->
                EvidenceAttachmentCard(attachment, onUiEvent)
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().testTag("evidence-label-input"),
                value = uiState.evidenceLabel,
                onValueChange = { onUiEvent(ActualProfileUpdateUiEvent.EvidenceLabelChanged(it)) },
                label = { Text(stringResource(Res.string.actual_evidence_label)) },
                placeholder = { Text(stringResource(Res.string.actual_evidence_placeholder)) },
                supportingText = if (uiState.evidenceLabel.isBlank() && uiState.evidenceAttachment == null) {
                    {
                        Text(
                            if (uiState.showEvidenceLabelError) {
                                stringResource(Res.string.actual_title_or_file)
                            } else {
                                stringResource(Res.string.actual_title_or_file_instead)
                            },
                        )
                    }
                } else {
                    null
                },
                isError = uiState.showEvidenceLabelError,
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().testTag("evidence-reference-input"),
                value = uiState.evidenceReference,
                onValueChange = { onUiEvent(ActualProfileUpdateUiEvent.EvidenceReferenceChanged(it)) },
                label = { Text(stringResource(Res.string.actual_reference_label)) },
                placeholder = { Text(stringResource(Res.string.actual_reference_placeholder)) },
                singleLine = true,
            )
            attachmentError?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.testTag("evidence-attachment-error"),
                    color = AppTheme.colors.status.error,
                    style = AppTheme.typography.bodyMedium,
                )
            }
            Text(
                stringResource(Res.string.actual_local_notice),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun EvidenceAttachmentCard(
    attachment: EvidenceAttachmentUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("evidence-attachment-card"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                attachment.displayName,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodyLarge,
            )
            Text(
                listOfNotNull(attachment.mimeType, attachment.byteSize.toReadableSize()).joinToString(" · "),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                if (attachment.isStored) stringResource(Res.string.actual_file_stored) else stringResource(Res.string.actual_file_pending),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
            )
            if (attachment.isStored) {
                TextButton(
                    modifier = Modifier.testTag("open-evidence-attachment"),
                    onClick = { onUiEvent(ActualProfileUpdateUiEvent.OpenEvidenceAttachment) },
                ) {
                    Text(stringResource(Res.string.actual_open_file))
                }
            }
            TextButton(
                modifier = Modifier.testTag("remove-evidence-attachment"),
                onClick = { onUiEvent(ActualProfileUpdateUiEvent.RemoveEvidenceAttachment) },
            ) {
                Text(stringResource(Res.string.actual_remove_file))
            }
        }
    }
}

@Composable
internal fun ConfirmationCard() {
    Text(
        stringResource(Res.string.actual_confirm_boundary),
        color = AppTheme.colors.text.secondary,
        style = AppTheme.typography.bodyMedium,
    )
}

@Composable
internal fun FormCard(
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

private fun Long.toReadableSize(): String = when {
    this < 1024L -> "$this B"
    this < 1024L * 1024L -> "${this / 1024L} KB"
    else -> "${this / (1024L * 1024L)} MB"
}
