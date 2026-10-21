package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme

@Composable
fun ProfileSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel,
    onCompleted: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onCompleted()
            viewModel.onUiEvent(ProfileSetupUiEvent.SaveHandled)
        }
    }

    ProfileSetupScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun ProfileSetupScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = true,
        title = "Build your map",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            ProfileSetupHeader()
            StudyLevelField(uiState = uiState, onUiEvent = onUiEvent)
            ProfileTextFields(uiState = uiState, onUiEvent = onUiEvent)
            DoorRelevantFacts(uiState = uiState, onUiEvent = onUiEvent)
            LocalDataNotice()

            uiState.error?.let { error ->
                Surface(
                    color = AppTheme.colors.status.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
                        color = AppTheme.colors.status.error,
                    )
                }
            }

            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile-setup-save"),
                text = "Create my map",
                enabled = uiState.canSave,
                isLoading = uiState.isSaving,
                onClick = { onUiEvent(ProfileSetupUiEvent.Save) },
            )
            Text(
                text = "You can add experience, projects, and evidence later. Unknown facts stay unknown.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ProfileSetupHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "MINIMUM PROFILE",
            color = AppTheme.colors.primary,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
        )
        Text(
            text = "Start with facts, not a perfect CV",
            color = AppTheme.colors.text.primary,
            style = AppTheme.typography.h3,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "These facts let Porta evaluate published hard rules. They never become an admission prediction.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyExtraLarge,
        )
    }
}

@Composable
private fun StudyLevelField(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
) {
    FormSection(
        title = "Current study level",
        description = "Choose the level you are enrolled in now.",
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            StudyLevelOption.entries.forEach { option ->
                FilterChip(
                    modifier = Modifier.testTag("profile-level-${option.storedValue}"),
                    selected = uiState.studyLevel == option,
                    onClick = { onUiEvent(ProfileSetupUiEvent.StudyLevelChanged(option)) },
                    label = { Text(option.label) },
                )
            }
        }
    }
}

@Composable
private fun ProfileTextFields(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
) {
    FormSection(
        title = "Academic facts",
        description = "Use your current values. IELTS is optional if you do not know it yet.",
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile-field-of-study"),
            value = uiState.fieldOfStudy,
            onValueChange = { onUiEvent(ProfileSetupUiEvent.FieldOfStudyChanged(it)) },
            label = { Text("Field of study") },
            placeholder = { Text("Computer Science") },
            singleLine = true,
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile-nationality"),
            value = uiState.nationality,
            onValueChange = { onUiEvent(ProfileSetupUiEvent.NationalityChanged(it)) },
            label = { Text("Nationality") },
            placeholder = { Text("VN") },
            supportingText = { Text("Country name or two-letter code") },
            singleLine = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .testTag("profile-gpa"),
                value = uiState.gpa,
                onValueChange = { onUiEvent(ProfileSetupUiEvent.GpaChanged(it)) },
                label = { Text("GPA") },
                placeholder = { Text("3.6") },
                suffix = { Text("/ 4.0") },
                supportingText = { uiState.gpaError?.let { Text(it) } },
                isError = uiState.gpaError != null,
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .testTag("profile-ielts"),
                value = uiState.ielts,
                onValueChange = { onUiEvent(ProfileSetupUiEvent.IeltsChanged(it)) },
                label = { Text("IELTS") },
                placeholder = { Text("Optional") },
                supportingText = { uiState.ieltsError?.let { Text(it) } },
                isError = uiState.ieltsError != null,
                singleLine = true,
            )
        }
    }
}

@Composable
private fun DoorRelevantFacts(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
) {
    FormSection(
        title = "Door-relevant facts",
        description = "Optional facts below make the sourced door map and Action Frontier more specific.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f).testTag("profile-age"),
                value = uiState.age,
                onValueChange = { onUiEvent(ProfileSetupUiEvent.AgeChanged(it)) },
                label = { Text("Age") },
                placeholder = { Text("Optional") },
                supportingText = { uiState.ageError?.let { Text(it) } },
                isError = uiState.ageError != null,
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f).testTag("profile-experience-months"),
                value = uiState.experienceMonths,
                onValueChange = { onUiEvent(ProfileSetupUiEvent.ExperienceMonthsChanged(it)) },
                label = { Text("Experience") },
                placeholder = { Text("Optional") },
                suffix = { Text("months") },
                supportingText = { uiState.experienceMonthsError?.let { Text(it) } },
                isError = uiState.experienceMonthsError != null,
                singleLine = true,
            )
        }

        BooleanFactField(
            label = "Currently enrolled",
            value = uiState.isEnrolled,
            includeUnknown = false,
            onChanged = { value -> value?.let { onUiEvent(ProfileSetupUiEvent.EnrolledChanged(it)) } },
        )
        BooleanFactField(
            label = "First degree completed",
            value = uiState.isDegreeCompleted,
            includeUnknown = true,
            onChanged = { onUiEvent(ProfileSetupUiEvent.DegreeCompletedChanged(it)) },
        )
        BooleanFactField(
            label = "Available for the full programme period",
            value = uiState.isAvailableFullPeriod,
            includeUnknown = true,
            onChanged = { onUiEvent(ProfileSetupUiEvent.AvailableFullPeriodChanged(it)) },
        )
    }
}

@Composable
private fun BooleanFactField(
    label: String,
    value: Boolean?,
    includeUnknown: Boolean,
    onChanged: (Boolean?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = value == true,
                onClick = { onChanged(true) },
                label = { Text("Yes") },
            )
            FilterChip(
                selected = value == false,
                onClick = { onChanged(false) },
                label = { Text("No") },
            )
            if (includeUnknown) {
                FilterChip(
                    selected = value == null,
                    onClick = { onChanged(null) },
                    label = { Text("Not sure") },
                )
            }
        }
    }
}

@Composable
private fun FormSection(
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
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = AppTheme.typography.h6, color = AppTheme.colors.text.primary)
                Text(
                    description,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.secondary,
                )
            }
            content()
        }
    }
}

@Composable
private fun LocalDataNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "LOCAL-FIRST",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "This profile stays on this device in the contest MVP. Porta does not upload it.",
                color = AppTheme.colors.text.primary,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileSetupScreenPreview() {
    AppTheme {
        ProfileSetupScreen(
            uiState = ProfileSetupUiState(
                studyLevel = StudyLevelOption.UNDERGRADUATE,
                fieldOfStudy = "Computer Science",
                nationality = "VN",
                gpa = "3.6",
                ielts = "6.5",
                age = "20",
                experienceMonths = "6",
                isDegreeCompleted = false,
                isAvailableFullPeriod = false,
            ),
            onUiEvent = {},
            onNavigateBack = {},
        )
    }
}
