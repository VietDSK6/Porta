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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
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

@Composable
fun ActualProfileUpdateScreen(
    viewModel: ActualProfileUpdateViewModel,
    onNavigateBack: () -> Unit,
    onProfileUpdated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val evidencePicker = rememberFilePickerLauncher(
        type = FileKitType.File(EvidenceAttachmentPolicy.allowedExtensions),
        mode = FileKitMode.Single,
    ) { file ->
        file ?: return@rememberFilePickerLauncher
        coroutineScope.launch {
            runCatching {
                val reportedSize = file.size()
                require(reportedSize > 0L) { "Choose a non-empty evidence file." }
                require(reportedSize <= EvidenceAttachmentPolicy.MAX_BYTE_SIZE) {
                    "Choose a file no larger than 10 MB."
                }
                val bytes = file.readBytes()
                require(bytes.isNotEmpty()) { "Choose a non-empty evidence file." }
                require(bytes.size.toLong() <= EvidenceAttachmentPolicy.MAX_BYTE_SIZE) {
                    "Choose a file no larger than 10 MB."
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
                            error.message ?: "Porta could not read this local file.",
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
        title = "Update actual profile",
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
    onPickEvidenceFile: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
        BoundaryBanner()
        ConfirmationChecklist(uiState)
        ValueComparisonCard(uiState)
        AchievedValueCard(uiState, onUiEvent)
        EvidenceCard(uiState, onUiEvent, onPickEvidenceFile)
        uiState.scopeWarning?.let { warning ->
            Text(
                text = warning,
                modifier = Modifier.testTag("actual-profile-update-scope-warning"),
                color = AppTheme.colors.status.warning,
                style = AppTheme.typography.bodyMedium,
            )
        }
        ConfirmationCard()
        uiState.error?.let { error ->
            Text(
                error,
                modifier = Modifier.testTag("actual-profile-update-error"),
                color = AppTheme.colors.status.error,
                style = AppTheme.typography.bodyMedium,
            )
        }
        uiState.confirmationGuidance?.let { guidance ->
            Text(
                text = guidance,
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
private fun ConfirmationChecklist(uiState: ActualProfileUpdateUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("actual-result-checklist"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Complete these steps", style = AppTheme.typography.h6)
            ChecklistStep(
                number = 1,
                title = if (uiState.inputKind == ActualValueInputKind.BOOLEAN) {
                    "Select actual result"
                } else {
                    "Enter actual result"
                },
                status = if (uiState.hasActualResult) "Done" else "Next",
                isDone = uiState.hasActualResult,
            )
            val evidenceStatus = when {
                uiState.hasSupportingEvidence -> "Added"

                uiState.inputKind == ActualValueInputKind.BOOLEAN && uiState.achievedBoolean == false ->
                    "Not needed for No"

                uiState.isEvidenceRequired &&
                    uiState.inputKind == ActualValueInputKind.BOOLEAN &&
                    uiState.achievedBoolean == null -> "Required if result is Yes"

                uiState.requiresSupportingEvidence -> "Required"

                else -> "Optional"
            }
            ChecklistStep(
                number = 2,
                title = "Add supporting evidence",
                status = evidenceStatus,
                isDone = uiState.hasSupportingEvidence ||
                    (uiState.inputKind == ActualValueInputKind.BOOLEAN && uiState.achievedBoolean == false),
            )
        }
    }
}

@Composable
private fun ChecklistStep(
    number: Int,
    title: String,
    status: String,
    isDone: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isDone) "✓" else number.toString(),
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.h6,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = AppTheme.typography.bodyLarge)
            Text(status, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
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
            uiState.scopeDescription?.let { scope ->
                Text(
                    text = scope,
                    color = AppTheme.colors.primary,
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
        description = "Choose or enter what is true now. The planned target is only a reference.",
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
            ActualValueInputKind.ENUM,
            ActualValueInputKind.ENUM_LIST,
            ActualValueInputKind.TEXT,
            ActualValueInputKind.DATE,
            ActualValueInputKind.DURATION,
            -> OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("achieved-value-input")
                    .semantics {
                        uiState.inputKind.inputStateDescription(uiState.unitLabel)?.let {
                            stateDescription = it
                        }
                    },
                value = uiState.achievedValue,
                onValueChange = { onUiEvent(ActualProfileUpdateUiEvent.AchievedValueChanged(it)) },
                label = { Text(uiState.inputKind.inputLabel()) },
                placeholder = uiState.inputKind.inputPlaceholder()?.let { placeholder ->
                    { Text(placeholder) }
                },
                suffix = uiState.unitLabel.takeIf(String::isNotBlank)?.let { unit ->
                    { Text(unit) }
                },
                supportingText = (uiState.achievedValueError ?: uiState.inputKind.inputGuidance())?.let { message ->
                    { Text(message) }
                },
                isError = uiState.achievedValueError != null,
                keyboardOptions = KeyboardOptions(keyboardType = uiState.inputKind.keyboardType()),
                singleLine = uiState.inputKind != ActualValueInputKind.ENUM_LIST,
                minLines = if (uiState.inputKind == ActualValueInputKind.ENUM_LIST) 2 else 1,
            )
        }
    }
}

private fun ActualValueInputKind.inputLabel(): String = when (this) {
    ActualValueInputKind.NUMBER -> "Achieved number"
    ActualValueInputKind.BOOLEAN -> "Achieved value"
    ActualValueInputKind.ENUM -> "Achieved option"
    ActualValueInputKind.ENUM_LIST -> "Achieved values"
    ActualValueInputKind.TEXT -> "Achieved value"
    ActualValueInputKind.DATE -> "Actual date"
    ActualValueInputKind.DURATION -> "Actual duration"
}

private fun ActualValueInputKind.inputPlaceholder(): String? = when (this) {
    ActualValueInputKind.DATE -> "YYYY-MM-DD"
    ActualValueInputKind.ENUM_LIST -> "First value, second value"
    else -> null
}

private fun ActualValueInputKind.inputGuidance(): String? = when (this) {
    ActualValueInputKind.DATE -> "Use an ISO date in YYYY-MM-DD format"
    ActualValueInputKind.DURATION -> "Enter a whole number; the plan unit is preserved"
    ActualValueInputKind.ENUM_LIST -> "Separate multiple values with commas or new lines"
    else -> null
}

private fun ActualValueInputKind.inputStateDescription(unitLabel: String): String? = when (this) {
    ActualValueInputKind.DATE -> "Expected date format YYYY-MM-DD"
    ActualValueInputKind.DURATION -> "Duration unit: $unitLabel"
    ActualValueInputKind.ENUM_LIST -> "Separate values with commas or new lines"
    else -> null
}

private fun ActualValueInputKind.keyboardType(): KeyboardType = when (this) {
    ActualValueInputKind.NUMBER -> KeyboardType.Decimal
    ActualValueInputKind.DURATION -> KeyboardType.Number
    else -> KeyboardType.Text
}

@Composable
private fun EvidenceCard(
    uiState: ActualProfileUpdateUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
    onPickEvidenceFile: () -> Unit,
) {
    val description = when {
        uiState.isEvidenceRequired &&
            uiState.inputKind == ActualValueInputKind.BOOLEAN &&
            uiState.achievedBoolean == null ->
            "Select Yes or No first. Supporting evidence is required only for a Yes result."

        uiState.isEvidenceRequired &&
            uiState.inputKind == ActualValueInputKind.BOOLEAN &&
            uiState.achievedBoolean == false ->
            "No supporting evidence is required for a No result."

        uiState.requiresSupportingEvidence ->
            "Add a short title or attach a file. A file is recommended and stays on this device."

        else -> "Store an optional local note or attach a file that supports this fact."
    }
    FormCard(
        title = "Supporting evidence",
        description = description,
    ) {
        if (uiState.requiresSupportingEvidence) {
            Text(
                text = "Required for a Yes result: add a title or file",
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
                Text("Add evidence details", style = AppTheme.typography.bodyLarge)
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
                        uiState.evidenceAttachment != null -> "Replace attached file"
                        uiState.requiresSupportingEvidence -> "Attach image or file (recommended)"
                        else -> "Attach image or file"
                    },
                )
            }
            Text(
                "Accepted: images, PDF, TXT, DOC, or DOCX · maximum 10 MB",
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
                label = { Text("Evidence title") },
                placeholder = { Text("Calendar approval, transcript, certificate…") },
                supportingText = if (uiState.evidenceLabel.isBlank() && uiState.evidenceAttachment == null) {
                    {
                        Text(
                            if (uiState.showEvidenceLabelError) {
                                "Add a short title or attach a file"
                            } else {
                                "Add a short title, or attach a file instead"
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
                label = { Text("Reference or local note") },
                placeholder = { Text("Optional file name, URL, or note") },
                singleLine = true,
            )
            uiState.evidenceAttachmentError?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.testTag("evidence-attachment-error"),
                    color = AppTheme.colors.status.error,
                    style = AppTheme.typography.bodyMedium,
                )
            }
            Text(
                "Stored locally on this device. Porta does not independently verify authenticity or submit it.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun EvidenceAttachmentCard(
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
                if (attachment.isStored) "Stored in Porta's private local storage" else "Ready to store when you confirm",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
            )
            if (attachment.isStored) {
                TextButton(
                    modifier = Modifier.testTag("open-evidence-attachment"),
                    onClick = { onUiEvent(ActualProfileUpdateUiEvent.OpenEvidenceAttachment) },
                ) {
                    Text("Open file")
                }
            }
            TextButton(
                modifier = Modifier.testTag("remove-evidence-attachment"),
                onClick = { onUiEvent(ActualProfileUpdateUiEvent.RemoveEvidenceAttachment) },
            ) {
                Text("Remove file")
            }
        }
    }
}

@Composable
private fun ConfirmationCard() {
    Text(
        "This updates your self-reported profile and rechecks affected rules. " +
            "Porta does not independently verify your evidence or predict selection.",
        color = AppTheme.colors.text.secondary,
        style = AppTheme.typography.bodyMedium,
    )
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

private fun Long.toReadableSize(): String = when {
    this < 1024L -> "$this B"
    this < 1024L * 1024L -> "${this / 1024L} KB"
    else -> "${this / (1024L * 1024L)} MB"
}

@Preview(widthDp = 390, heightDp = 844)
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
                includeEvidence = true,
                evidenceLabel = "Official IELTS score report",
                evidenceAttachment = EvidenceAttachmentUiState(
                    displayName = "ielts-score-report.pdf",
                    mimeType = "application/pdf",
                    byteSize = 458_752L,
                    isStored = true,
                ),
            ),
            onUiEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun EvidenceAttachmentCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
        ) {
            EvidenceCard(
                uiState = ActualProfileUpdateUiState(
                    isLoading = false,
                    hasCompletedPlan = true,
                    includeEvidence = true,
                    evidenceLabel = "Official IELTS score report",
                    evidenceAttachment = EvidenceAttachmentUiState(
                        displayName = "ielts-score-report.pdf",
                        mimeType = "application/pdf",
                        byteSize = 458_752L,
                        isStored = true,
                    ),
                ),
                onUiEvent = {},
                onPickEvidenceFile = {},
            )
        }
    }
}
