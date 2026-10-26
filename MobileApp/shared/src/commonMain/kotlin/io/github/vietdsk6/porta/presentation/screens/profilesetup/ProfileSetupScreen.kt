package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
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
import io.github.vietdsk6.porta.domain.model.FactKey

@Composable
fun ProfileSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel,
    onCompleted: () -> Unit,
    onNavigateBack: () -> Unit,
    requestedFactKeyName: String? = null,
    requestContext: String? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val requestedFactKey = requestedFactKeyName?.let { name ->
        FactKey.entries.firstOrNull { it.name == name }
    }

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
        requestedFactKey = requestedFactKey,
        requestContext = requestContext,
    )
}

@Composable
fun ProfileSetupScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    requestedFactKey: FactKey? = null,
    requestContext: String? = null,
) {
    var expandedSectionName by rememberSaveable(requestedFactKey?.name) {
        mutableStateOf(requestedFactKey?.setupSection()?.name)
    }

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
            requestedFactKey?.let { factKey ->
                RequestedFactNotice(
                    factKey = factKey,
                    requestContext = requestContext,
                )
            }
            EssentialsSection(uiState = uiState, onUiEvent = onUiEvent)
            AdditionalFactsIntro()
            AcademicRecordSection(
                uiState = uiState,
                onUiEvent = onUiEvent,
                isExpanded = expandedSectionName == ProfileSetupSection.ACADEMIC.name,
                onToggle = {
                    expandedSectionName = ProfileSetupSection.ACADEMIC.name
                        .takeUnless { it == expandedSectionName }
                },
            )
            ExperienceEvidenceSection(
                uiState = uiState,
                onUiEvent = onUiEvent,
                isExpanded = expandedSectionName == ProfileSetupSection.EXPERIENCE.name,
                onToggle = {
                    expandedSectionName = ProfileSetupSection.EXPERIENCE.name
                        .takeUnless { it == expandedSectionName }
                },
            )
            ContextSection(
                uiState = uiState,
                onUiEvent = onUiEvent,
                isExpanded = expandedSectionName == ProfileSetupSection.CONTEXT.name,
                onToggle = {
                    expandedSectionName = ProfileSetupSection.CONTEXT.name
                        .takeUnless { it == expandedSectionName }
                },
            )
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
            if (!uiState.canSave && !uiState.isSaving) {
                Text(
                    text = uiState.saveDisabledReason.orEmpty(),
                    modifier = Modifier.testTag("profile-setup-save-reason"),
                    color = AppTheme.colors.status.error,
                    style = AppTheme.typography.bodyMedium,
                )
            }
            Text(
                text = "Blank fields stay not provided. They are never interpreted as false or failed.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RequestedFactNotice(
    factKey: FactKey,
    requestContext: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("profile-requested-${factKey.name.lowercase()}"),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "WHY PORTA IS ASKING",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
            )
            Text(
                text = "Add ${factKey.profileFieldLabel()} for the opportunity you were reviewing.",
                color = AppTheme.colors.text.primary,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            requestContext?.takeIf(String::isNotBlank)?.let { context ->
                Text(
                    text = context,
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
            }
            Text(
                text = "This fills a missing check. It does not claim that your application is competitive.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AdditionalFactsIntro() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Add only what helps",
            style = AppTheme.typography.h5,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Everything below is optional. If a value is missing, checks that depend on it stay marked as not checked.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
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
    }
}

@Composable
private fun AcademicRecordSection(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    ProgressiveFormSection(
        title = "Academic record",
        description = "Keep the original GPA scale. Porta never silently converts it.",
        status = uiState.academicProvidedCount.providedStatus(total = 4),
        isExpanded = isExpanded,
        onToggle = onToggle,
        testTag = "profile-section-academic",
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
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-gpa"),
            value = uiState.gpa,
            onValueChange = { onUiEvent(ProfileSetupUiEvent.GpaChanged(it)) },
            label = { Text("Your GPA") },
            placeholder = { Text("3.6") },
            suffix = { Text("/ ${uiState.gpaScale.label}") },
            supportingText = { uiState.gpaError?.let { Text(it) } },
            isError = uiState.gpaError != null,
            singleLine = true,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-ielts"),
            value = uiState.ielts,
            onValueChange = { onUiEvent(ProfileSetupUiEvent.IeltsChanged(it)) },
            label = { Text("IELTS overall band") },
            placeholder = { Text("Not provided") },
            supportingText = { uiState.ieltsError?.let { Text(it) } },
            isError = uiState.ieltsError != null,
            singleLine = true,
        )
        BooleanFactField(
            label = "Currently enrolled",
            value = uiState.isEnrolled,
            includeUnknown = true,
            onChanged = { onUiEvent(ProfileSetupUiEvent.EnrolledChanged(it)) },
        )
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
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    ProgressiveFormSection(
        title = "Experience and evidence",
        description = "Growth evidence adds context. It never passes a published check automatically.",
        status = uiState.experienceProvidedCount.providedStatus(total = 3),
        isExpanded = isExpanded,
        onToggle = onToggle,
        testTag = "profile-section-experience",
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
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    ProgressiveFormSection(
        title = "Context",
        description = "Used only when an official rule depends on it; never suggested as something to change.",
        status = uiState.contextProvidedCount.providedStatus(total = 4),
        isExpanded = isExpanded,
        onToggle = onToggle,
        testTag = "profile-section-context",
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
            modifier = Modifier.fillMaxWidth().testTag("profile-date-of-birth"),
            value = uiState.dateOfBirth,
            onValueChange = { onUiEvent(ProfileSetupUiEvent.DateOfBirthChanged(it)) },
            label = { Text("Date of birth") },
            placeholder = { Text("YYYY-MM-DD") },
            supportingText = {
                Text(
                    uiState.dateOfBirthError
                        ?: "Used only when a source checks age on a named reference date.",
                )
            },
            isError = uiState.dateOfBirthError != null,
            singleLine = true,
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
private fun ProgressiveFormSection(
    title: String,
    description: String,
    status: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.32f)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onToggle)
                    .padding(AppTheme.spacing.defaultSpacing),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = title,
                        style = AppTheme.typography.h6,
                        color = AppTheme.colors.text.primary,
                    )
                    Text(
                        text = if (isExpanded) description else status,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.secondary,
                    )
                }
                Text(
                    text = when {
                        isExpanded -> "Hide"
                        status.startsWith("All") -> "Review"
                        else -> "Add"
                    },
                    color = AppTheme.colors.primary,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (isExpanded) {
                HorizontalDivider(
                    color = AppTheme.colors.outline.copy(alpha = 0.24f),
                )
                Column(
                    modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
                ) {
                    content()
                }
            }
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

private enum class ProfileSetupSection {
    ACADEMIC,
    EXPERIENCE,
    CONTEXT,
}

private fun FactKey.setupSection(): ProfileSetupSection? = when (this) {
    FactKey.GPA,
    FactKey.IELTS_SCORE,
    FactKey.ENROLLED,
    FactKey.DEGREE_COMPLETED,
    -> ProfileSetupSection.ACADEMIC

    FactKey.EXPERIENCE_MONTHS,
    FactKey.COMMUNITY_PROJECT,
    FactKey.LEADERSHIP,
    -> ProfileSetupSection.EXPERIENCE

    FactKey.NATIONALITY,
    FactKey.RESIDENCY,
    FactKey.AGE,
    FactKey.DATE_OF_BIRTH,
    -> ProfileSetupSection.CONTEXT

    FactKey.STUDY_LEVEL,
    FactKey.FIELD_OF_STUDY,
    FactKey.AVAILABLE_FULL_PERIOD,
    -> null
}

private fun FactKey.profileFieldLabel(): String = when (this) {
    FactKey.GPA -> "your GPA"
    FactKey.STUDY_LEVEL -> "your study level"
    FactKey.FIELD_OF_STUDY -> "your field of study"
    FactKey.NATIONALITY -> "your nationality"
    FactKey.RESIDENCY -> "your country of residence"
    FactKey.IELTS_SCORE -> "your IELTS score"
    FactKey.EXPERIENCE_MONTHS -> "your relevant experience duration"
    FactKey.COMMUNITY_PROJECT -> "a community project"
    FactKey.LEADERSHIP -> "leadership evidence"
    FactKey.ENROLLED -> "your current enrollment status"
    FactKey.AGE -> "your age"
    FactKey.DATE_OF_BIRTH -> "your date of birth"
    FactKey.DEGREE_COMPLETED -> "your degree completion status"
    FactKey.AVAILABLE_FULL_PERIOD -> "programme-specific availability"
}

private fun Int.providedStatus(total: Int): String = when (this) {
    0 -> "Not checked yet · optional"
    total -> "All $total values provided"
    else -> "$this of $total values provided · missing checks stay unknown"
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

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun RequestedProfileFactScreenPreview() {
    AppTheme {
        ProfileSetupScreen(
            uiState = ProfileSetupUiState(
                studyLevel = StudyLevelOption.UNDERGRADUATE,
                fieldOfStudy = "Computer Science",
            ),
            onUiEvent = {},
            onNavigateBack = {},
            requestedFactKey = FactKey.DEGREE_COMPLETED,
            requestContext = "Applicants must hold a bachelor's degree or equivalent.",
        )
    }
}
