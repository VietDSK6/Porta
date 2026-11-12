package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.util.file.EvidenceAttachmentPolicy
import io.github.vietdsk6.porta.util.file.SelectedEvidenceFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActualProfileUpdateScreen(
    viewModel: ActualProfileUpdateViewModel,
    onNavigateBack: () -> Unit,
    onProfileUpdated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val missingFileMessage = stringResource(Res.string.actual_missing_file)
    val tooLargeMessage = stringResource(Res.string.actual_file_too_large)
    val readFailureMessage = stringResource(Res.string.actual_file_read_failed)
    val evidencePicker = rememberFilePickerLauncher(
        type = FileKitType.File(EvidenceAttachmentPolicy.allowedExtensions),
        mode = FileKitMode.Single,
    ) { file ->
        file ?: return@rememberFilePickerLauncher
        coroutineScope.launch {
            runCatching {
                val reportedSize = file.size()
                require(reportedSize > 0L) { missingFileMessage }
                require(reportedSize <= EvidenceAttachmentPolicy.MAX_BYTE_SIZE) {
                    tooLargeMessage
                }
                val bytes = file.readBytes()
                require(bytes.isNotEmpty()) { missingFileMessage }
                require(bytes.size.toLong() <= EvidenceAttachmentPolicy.MAX_BYTE_SIZE) {
                    tooLargeMessage
                }
                SelectedEvidenceFile(
                    displayName = file.name,
                    mimeType = file.mimeType()?.toString(),
                    bytes = bytes,
                )
            }.fold(
                onSuccess = { selectedFile ->
                    viewModel.onUiEvent(ActualProfileUpdateUiEvent.EvidenceFileSelected(selectedFile))
                },
                onFailure = { error ->
                    viewModel.onUiEvent(
                        ActualProfileUpdateUiEvent.EvidenceFileSelectionFailed(
                            error.message ?: readFailureMessage,
                        ),
                    )
                },
            )
        }
    }

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
        onPickEvidenceFile = evidencePicker::launch,
    )
}

@Composable
fun ActualProfileUpdateScreen(
    uiState: ActualProfileUpdateUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onPickEvidenceFile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("actual-profile-update-screen"),
        isScrollableContent = true,
        title = stringResource(Res.string.actual_title),
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        when {
            uiState.isLoading -> LoadingState()
            !uiState.hasCompletedPlan -> UnavailableState(onNavigateBack)
            else -> UpdateForm(uiState, onUiEvent, onNavigateBack, onPickEvidenceFile)
        }
    }
}

@Composable
internal fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            stringResource(Res.string.actual_reading_plan),
            modifier = Modifier.padding(top = 12.dp),
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
internal fun UnavailableState(onNavigateBack: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(Res.string.actual_no_completed), style = AppTheme.typography.h5)
            Text(
                stringResource(Res.string.actual_no_plan_help),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodyLarge,
            )
            Button(modifier = Modifier.fillMaxWidth(), onClick = onNavigateBack) {
                Text(stringResource(Res.string.actual_return_plan))
            }
        }
    }
}

@Composable
internal fun UpdateForm(
    uiState: ActualProfileUpdateUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onPickEvidenceFile: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
        BoundaryBanner()
        ConfirmationChecklist(uiState)
        ValueComparisonCard(uiState)
        AchievedValueCard(uiState, onUiEvent)
        EvidenceCard(uiState, onUiEvent, onPickEvidenceFile)
        val scopeWarning = uiState.localizedScopeWarning()
        scopeWarning?.let { warning ->
            Text(
                text = warning,
                modifier = Modifier.testTag("actual-profile-update-scope-warning"),
                color = AppTheme.colors.status.warning,
                style = AppTheme.typography.bodyMedium,
            )
        }
        ConfirmationCard()
        val displayError = uiState.localizedError()
        displayError?.let { error ->
            Text(
                error,
                modifier = Modifier.testTag("actual-profile-update-error"),
                color = AppTheme.colors.status.error,
                style = AppTheme.typography.bodyMedium,
            )
        }
        uiState.confirmationGuidanceCode?.let { guidance ->
            Text(
                text = when (guidance) {
                    ConfirmationGuidance.REVIEW_SOURCE -> stringResource(Res.string.actual_guidance_source)
                    ConfirmationGuidance.SELECT_BOOLEAN -> stringResource(Res.string.actual_guidance_boolean)
                    ConfirmationGuidance.ENTER_RESULT -> stringResource(Res.string.actual_guidance_result)
                    ConfirmationGuidance.FIX_RESULT -> stringResource(Res.string.actual_guidance_fix)
                    ConfirmationGuidance.ADD_EVIDENCE -> stringResource(Res.string.actual_guidance_evidence)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("actual-profile-update-guidance"),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
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
                Text(stringResource(Res.string.actual_confirm_update))
            }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().testTag("cancel-actual-profile-update"),
            onClick = onNavigateBack,
        ) {
            Text(stringResource(Res.string.actual_keep_unchanged))
        }
        Spacer(Modifier.height(8.dp))
    }
}
