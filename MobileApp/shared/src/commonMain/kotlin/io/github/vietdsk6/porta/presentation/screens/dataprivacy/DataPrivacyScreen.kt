package io.github.vietdsk6.porta.presentation.screens.dataprivacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.presentation.navigation.DesktopCommand
import io.github.vietdsk6.porta.presentation.navigation.LocalDesktopCommand
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileDataAction
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileImportFactUiState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileImportPreviewUiState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiEvent
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileViewModel
import io.github.vietdsk6.porta.presentation.screens.studentprofile.confirmationBody
import io.github.vietdsk6.porta.presentation.screens.studentprofile.confirmationButton
import io.github.vietdsk6.porta.presentation.screens.studentprofile.confirmationTitle
import io.github.vietdsk6.porta.presentation.screens.studentprofile.toText
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun DataPrivacyScreen(
    viewModel: StudentProfileViewModel,
    onNavigateBack: () -> Unit,
    onOpenPackInspector: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val profilePicker = rememberFilePickerLauncher(
        type = FileKitType.File("json"),
        mode = FileKitMode.Single,
    ) { file ->
        file ?: return@rememberFilePickerLauncher
        coroutineScope.launch {
            runCatching { file.readString() }.fold(
                onSuccess = { encodedProfile ->
                    viewModel.onUiEvent(
                        StudentProfileUiEvent.ImportProfileSelected(
                            fileName = file.name,
                            encodedProfile = encodedProfile,
                        ),
                    )
                },
                onFailure = {
                    viewModel.onUiEvent(StudentProfileUiEvent.ImportProfileReadFailed)
                },
            )
        }
    }
    val desktopRequest = LocalDesktopCommand.current
    LaunchedEffect(desktopRequest?.command) {
        if (desktopRequest?.command == DesktopCommand.ImportProfile) {
            profilePicker.launch()
            desktopRequest.onHandled()
        }
    }
    DataPrivacyScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onOpenPackInspector = onOpenPackInspector,
        onImportProfile = profilePicker::launch,
        modifier = modifier,
    )
}

@Composable
fun DataPrivacyScreen(
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenPackInspector: () -> Unit,
    onImportProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.fillMaxSize().testTag("data-privacy-screen"),
        isScrollableContent = true,
        title = stringResource(Res.string.data_privacy_title),
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = stringResource(
                    if (uiState.supportsProfileImport) {
                        Res.string.data_privacy_intro_import
                    } else {
                        Res.string.data_privacy_intro_basic
                    },
                ),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )

            DataSection(title = stringResource(Res.string.data_privacy_review_transfer)) {
                DataActionRow(
                    title = stringResource(Res.string.data_privacy_pack_title),
                    description = stringResource(Res.string.data_privacy_pack_description),
                    action = stringResource(Res.string.data_privacy_inspect),
                    tag = "student-profile-pack-inspector",
                    enabled = !uiState.isWorking && !uiState.isLoading,
                    onClick = onOpenPackInspector,
                )
                HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.24f))
                DataActionRow(
                    title = stringResource(Res.string.data_privacy_metadata_title),
                    description = stringResource(Res.string.data_privacy_metadata_description),
                    action = stringResource(Res.string.data_privacy_export),
                    tag = "student-profile-export",
                    enabled = uiState.hasRealProfile && !uiState.isWorking && !uiState.isLoading,
                    onClick = { onUiEvent(StudentProfileUiEvent.ExportMetadata) },
                )
                if (uiState.supportsProfileImport) {
                    HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.24f))
                    DataActionRow(
                        title = stringResource(Res.string.data_privacy_copy_title),
                        description = stringResource(Res.string.data_privacy_copy_description),
                        action = stringResource(Res.string.data_privacy_import),
                        tag = "student-profile-import",
                        enabled = !uiState.isWorking && !uiState.isLoading,
                        onClick = onImportProfile,
                    )
                }
            }

            DataSection(title = stringResource(Res.string.data_privacy_remove_title)) {
                DataActionRow(
                    title = stringResource(Res.string.data_privacy_academic_title),
                    description = stringResource(Res.string.data_privacy_academic_description),
                    action = stringResource(Res.string.data_privacy_clear),
                    tag = "student-profile-clear",
                    enabled = uiState.hasRealProfile && !uiState.isWorking && !uiState.isLoading,
                    actionColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        onUiEvent(StudentProfileUiEvent.DataActionRequested(ProfileDataAction.CLEAR_PROFILE_AND_PLANS))
                    },
                )
                HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.24f))
                DataActionRow(
                    title = stringResource(Res.string.data_privacy_all_title),
                    description = stringResource(Res.string.data_privacy_all_description),
                    action = stringResource(Res.string.data_privacy_reset),
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
                            text = message.toText(),
                            modifier = Modifier.weight(1f),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.text.primary,
                        )
                        TextButton(onClick = { onUiEvent(StudentProfileUiEvent.MessageHandled) }) {
                            Text(stringResource(Res.string.data_privacy_dismiss))
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
                    Text(stringResource(Res.string.data_privacy_keep_data))
                }
            },
        )
    }

    uiState.pendingProfileImport?.let { preview ->
        ProfileImportPreviewDialog(
            preview = preview,
            isWorking = uiState.isWorking,
            error = uiState.profileImportError?.let { stringResource(Res.string.data_privacy_import_failed) },
            onConfirm = { onUiEvent(StudentProfileUiEvent.ImportProfileConfirmed) },
            onCancel = { onUiEvent(StudentProfileUiEvent.ImportProfileCancelled) },
        )
    }
}

@Composable
private fun ProfileImportPreviewDialog(
    preview: ProfileImportPreviewUiState,
    isWorking: Boolean,
    error: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag("student-profile-import-preview"),
        onDismissRequest = { if (!isWorking) onCancel() },
        title = { Text(stringResource(Res.string.data_privacy_import_confirm_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = preview.fileName,
                    modifier = Modifier.testTag("student-profile-import-file-name"),
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        Res.string.data_privacy_import_counts,
                        preview.facts.size,
                        preview.ruleAssertionCount,
                        preview.evidenceMetadataCount,
                    ),
                    modifier = Modifier.testTag("student-profile-import-counts"),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
                Surface(
                    color = AppTheme.colors.alternative,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.data_privacy_replace_warning),
                            fontWeight = FontWeight.Bold,
                            style = AppTheme.typography.bodySmall,
                        )
                        Text(
                            text = stringResource(Res.string.data_privacy_unchanged_notice),
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                }
                if (preview.sensitiveFactCount > 0 || preview.evidenceMetadataCount > 0) {
                    Text(
                        text = stringResource(
                            Res.string.data_privacy_sensitive_preview,
                            preview.sensitiveFactCount,
                            preview.evidenceMetadataCount,
                        ),
                        modifier = Modifier.testTag("student-profile-import-sensitive-warning"),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.status.warning,
                    )
                }
                Text(stringResource(Res.string.data_privacy_facts_in_copy), style = AppTheme.typography.h6)
                preview.facts.forEach { fact ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student-profile-import-fact-${fact.key.name.lowercase()}"),
                        color = AppTheme.colors.surfaceContainer,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(fact.label, fontWeight = FontWeight.SemiBold)
                            Text(fact.value, style = AppTheme.typography.bodyMedium)
                            Text(
                                text = listOfNotNull(
                                    fact.provenanceLabel,
                                    stringResource(Res.string.data_privacy_evidence_included)
                                        .takeIf { fact.hasEvidenceMetadata },
                                    stringResource(Res.string.data_privacy_sensitive_context)
                                        .takeIf { fact.isSensitive },
                                ).joinToString(" · "),
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.text.secondary,
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(Res.string.data_privacy_fingerprint_notice),
                    modifier = Modifier.testTag("student-profile-import-fingerprint-note"),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
                error?.let {
                    Text(
                        text = it,
                        modifier = Modifier.testTag("student-profile-import-error"),
                        color = MaterialTheme.colorScheme.error,
                        style = AppTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("student-profile-import-confirm"),
                enabled = !isWorking,
                onClick = onConfirm,
            ) {
                Text(stringResource(if (isWorking) Res.string.data_privacy_importing else Res.string.data_privacy_replace_profile))
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.testTag("student-profile-import-cancel"),
                enabled = !isWorking,
                onClick = onCancel,
            ) {
                Text(stringResource(Res.string.data_privacy_cancel))
            }
        },
    )
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
            uiState = StudentProfileUiState(
                isLoading = false,
                hasRealProfile = true,
                supportsProfileImport = true,
            ),
            onUiEvent = {},
            onNavigateBack = {},
            onOpenPackInspector = {},
        )
    }
}

@Preview(device = "spec:width=1280dp,height=1000dp,dpi=160")
@Composable
private fun DataPrivacyImportPreview() {
    AppTheme {
        DataPrivacyScreen(
            uiState = StudentProfileUiState(
                isLoading = false,
                hasRealProfile = true,
                supportsProfileImport = true,
                pendingProfileImport = ProfileImportPreviewUiState(
                    fileName = "porta-profile.json",
                    facts = listOf(
                        ProfileImportFactUiState(
                            key = FactKey.NATIONALITY,
                            label = "Nationality",
                            value = "Vietnam",
                            provenanceLabel = "Self-reported",
                            hasEvidenceMetadata = false,
                            isSensitive = true,
                        ),
                        ProfileImportFactUiState(
                            key = FactKey.IELTS_SCORE,
                            label = "IELTS score",
                            value = "7.0 band",
                            provenanceLabel = "Evidence-backed",
                            hasEvidenceMetadata = true,
                            isSensitive = false,
                        ),
                    ),
                    ruleAssertionCount = 3,
                    evidenceMetadataCount = 1,
                    sensitiveFactCount = 1,
                ),
            ),
            onUiEvent = {},
            onNavigateBack = {},
            onOpenPackInspector = {},
        )
    }
}
