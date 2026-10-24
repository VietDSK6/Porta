package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        title = "Edit academic profile",
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
            EssentialsSection(uiState = uiState, onUiEvent = onUiEvent)
            AcademicRecordSection(uiState = uiState, onUiEvent = onUiEvent)
            ExperienceEvidenceSection(uiState = uiState, onUiEvent = onUiEvent)
            ContextSection(uiState = uiState, onUiEvent = onUiEvent)
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
                text = "Save profile",
                enabled = uiState.canSave,
                isLoading = uiState.isSaving,
                onClick = { onUiEvent(ProfileSetupUiEvent.Save) },
            )
            Text(
                text = "Blank fields stay not provided. They are never interpreted as false or failed.",
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
            text = "YOUR SOURCE OF TRUTH",
            color = AppTheme.colors.primary,
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
        )
        Text(
            text = "Describe where you are now",
            color = AppTheme.colors.text.primary,
            style = AppTheme.typography.h3,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Porta checks published requirements against these values. It does not predict selection outcomes.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyExtraLarge,
        )
    }
}

@Composable
private fun EssentialsSection(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
) {
    FormSection(
        title = "Essentials",
        description = "The minimum context needed to build your door map.",
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
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-field-of-study"),
            value = uiState.fieldOfStudy,
            onValueChange = { onUiEvent(ProfileSetupUiEvent.FieldOfStudyChanged(it)) },
            label = { Text("Field of study") },
            placeholder = { Text("Computer Science") },
            singleLine = true,
        )
        BooleanFactField(
            label = "Currently enrolled",
            value = uiState.isEnrolled,
            includeUnknown = true,
            onChanged = { onUiEvent(ProfileSetupUiEvent.EnrolledChanged(it)) },
        )
    }
}

@Composable
private fun AcademicRecordSection(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
) {
    FormSection(
        title = "Academic record",
        description = "Keep the original GPA scale. Porta never silently converts it.",
    ) {
        Text("GPA scale", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GpaScaleOption.entries.forEach { option ->
                FilterChip(
                    selected = uiState.gpaScale == option,
                    onClick = { onUiEvent(ProfileSetupUiEvent.GpaScaleChanged(option)) },
                    label = { Text(option.label) },
                )
            }
        }
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
                suffix = { Text("/ ${uiState.gpaScale.label}") },
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
        BooleanFactField(
            label = "First degree completed",
            value = uiState.isDegreeCompleted,
            includeUnknown = true,
            onChanged = { onUiEvent(ProfileSetupUiEvent.DegreeCompletedChanged(it)) },
        )
    }
}

@Composable
private fun ExperienceEvidenceSection(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
) {
    FormSection(
        title = "Experience and evidence",
        description = "Growth evidence adds context. It never passes a published check automatically.",
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-experience-months"),
            value = uiState.experienceMonths,
            onValueChange = { onUiEvent(ProfileSetupUiEvent.ExperienceMonthsChanged(it)) },
            label = { Text("Relevant experience") },
            placeholder = { Text("Not provided") },
            suffix = { Text("months") },
            supportingText = { uiState.experienceMonthsError?.let { Text(it) } },
            isError = uiState.experienceMonthsError != null,
            singleLine = true,
        )

        GrowthEvidenceFields(
            heading = "Community project",
            firstLabel = "Project title",
            firstValue = uiState.communityProjectTitle,
            secondLabel = "Short summary",
            secondValue = uiState.communityProjectSummary,
            evidenceValue = uiState.communityProjectEvidenceReference,
            tagPrefix = "community-project",
            onFirstChanged = { onUiEvent(ProfileSetupUiEvent.CommunityProjectTitleChanged(it)) },
            onSecondChanged = { onUiEvent(ProfileSetupUiEvent.CommunityProjectSummaryChanged(it)) },
            onEvidenceChanged = { onUiEvent(ProfileSetupUiEvent.CommunityProjectEvidenceChanged(it)) },
        )
        GrowthEvidenceFields(
            heading = "Leadership",
            firstLabel = "Role",
            firstValue = uiState.leadershipRole,
            secondLabel = "Team or activity",
            secondValue = uiState.leadershipTitle,
            evidenceValue = uiState.leadershipEvidenceReference,
            tagPrefix = "leadership",
            onFirstChanged = { onUiEvent(ProfileSetupUiEvent.LeadershipRoleChanged(it)) },
            onSecondChanged = { onUiEvent(ProfileSetupUiEvent.LeadershipTitleChanged(it)) },
            onEvidenceChanged = { onUiEvent(ProfileSetupUiEvent.LeadershipEvidenceChanged(it)) },
        )
    }
}

@Composable
private fun ContextSection(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
) {
    FormSection(
        title = "Context",
        description = "Used only when an official rule depends on it; never suggested as something to change.",
    ) {
        CountrySelector(
            label = "Nationality",
            selectedCode = uiState.nationality,
            testTag = "profile-nationality",
            onSelected = { onUiEvent(ProfileSetupUiEvent.NationalityChanged(it)) },
        )
        CountrySelector(
            label = "Country of residence",
            selectedCode = uiState.residency,
            testTag = "profile-residency",
            onSelected = { onUiEvent(ProfileSetupUiEvent.ResidencyChanged(it)) },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-age"),
            value = uiState.age,
            onValueChange = { onUiEvent(ProfileSetupUiEvent.AgeChanged(it)) },
            label = { Text("Age") },
            placeholder = { Text("Not provided") },
            supportingText = { uiState.ageError?.let { Text(it) } },
            isError = uiState.ageError != null,
            singleLine = true,
        )
    }
}

@Composable
private fun GrowthEvidenceFields(
    heading: String,
    firstLabel: String,
    firstValue: String,
    secondLabel: String,
    secondValue: String,
    evidenceValue: String,
    tagPrefix: String,
    onFirstChanged: (String) -> Unit,
    onSecondChanged: (String) -> Unit,
    onEvidenceChanged: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = heading,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-$tagPrefix-primary"),
            value = firstValue,
            onValueChange = onFirstChanged,
            label = { Text(firstLabel) },
            placeholder = { Text("Not provided") },
            singleLine = true,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-$tagPrefix-secondary"),
            value = secondValue,
            onValueChange = onSecondChanged,
            label = { Text(secondLabel) },
            placeholder = { Text("Not provided") },
            minLines = 2,
            maxLines = 4,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-$tagPrefix-evidence"),
            value = evidenceValue,
            onValueChange = onEvidenceChanged,
            label = { Text("Evidence reference (optional)") },
            placeholder = { Text("URL, document name, or portfolio reference") },
            singleLine = true,
        )
    }
}

@Composable
private fun CountrySelector(
    label: String,
    selectedCode: String,
    testTag: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val selectedCountry = countryForStoredValue(selectedCode)
    val visibleCountries = remember(query) {
        val normalized = query.trim()
        ISO_COUNTRIES.filter { country ->
            normalized.isBlank() ||
                country.name.contains(normalized, ignoreCase = true) ||
                country.alpha2.contains(normalized, ignoreCase = true)
        }.take(20)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().testTag(testTag),
            onClick = { expanded = true },
        ) {
            Text(
                text = selectedCountry?.let { "$label · ${it.displayName}" } ?: "$label · Not provided",
                modifier = Modifier.fillMaxWidth(),
                color = if (selectedCountry == null) AppTheme.colors.text.secondary else AppTheme.colors.text.primary,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                query = ""
            },
            modifier = Modifier.widthIn(min = 300.dp, max = 520.dp).heightIn(max = 420.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(),
                value = query,
                onValueChange = { query = it },
                label = { Text("Search countries") },
                singleLine = true,
            )
            DropdownMenuItem(
                text = { Text("Not provided") },
                onClick = {
                    onSelected("")
                    expanded = false
                    query = ""
                },
            )
            visibleCountries.forEach { country ->
                DropdownMenuItem(
                    text = { Text(country.displayName) },
                    onClick = {
                        onSelected(country.alpha2)
                        expanded = false
                        query = ""
                    },
                )
            }
        }
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
                residency = "VN",
                gpa = "3.6",
                ielts = "6.5",
                age = "20",
                experienceMonths = "6",
                isEnrolled = true,
                isDegreeCompleted = false,
                communityProjectTitle = "Local coding club",
                communityProjectSummary = "Built a peer-led Kotlin workshop series.",
                leadershipRole = "Student mentor",
                leadershipTitle = "Faculty peer network",
            ),
            onUiEvent = {},
            onNavigateBack = {},
        )
    }
}
