package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_setup_academic
import io.github.vietdsk6.porta.generated.resources.profile_setup_academic_hint
import io.github.vietdsk6.porta.generated.resources.profile_setup_activity_example
import io.github.vietdsk6.porta.generated.resources.profile_setup_age_hint
import io.github.vietdsk6.porta.generated.resources.profile_setup_age_today
import io.github.vietdsk6.porta.generated.resources.profile_setup_community_project
import io.github.vietdsk6.porta.generated.resources.profile_setup_context
import io.github.vietdsk6.porta.generated.resources.profile_setup_context_hint
import io.github.vietdsk6.porta.generated.resources.profile_setup_currently_enrolled
import io.github.vietdsk6.porta.generated.resources.profile_setup_degree_completed
import io.github.vietdsk6.porta.generated.resources.profile_setup_essentials
import io.github.vietdsk6.porta.generated.resources.profile_setup_essentials_hint
import io.github.vietdsk6.porta.generated.resources.profile_setup_evidence_placeholder
import io.github.vietdsk6.porta.generated.resources.profile_setup_evidence_reference
import io.github.vietdsk6.porta.generated.resources.profile_setup_experience
import io.github.vietdsk6.porta.generated.resources.profile_setup_experience_evidence
import io.github.vietdsk6.porta.generated.resources.profile_setup_experience_hint
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_of_study
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_placeholder
import io.github.vietdsk6.porta.generated.resources.profile_setup_gpa_scale
import io.github.vietdsk6.porta.generated.resources.profile_setup_ielts
import io.github.vietdsk6.porta.generated.resources.profile_setup_leadership
import io.github.vietdsk6.porta.generated.resources.profile_setup_months
import io.github.vietdsk6.porta.generated.resources.profile_setup_nationality
import io.github.vietdsk6.porta.generated.resources.profile_setup_no
import io.github.vietdsk6.porta.generated.resources.profile_setup_not_provided
import io.github.vietdsk6.porta.generated.resources.profile_setup_project_example
import io.github.vietdsk6.porta.generated.resources.profile_setup_project_summary_example
import io.github.vietdsk6.porta.generated.resources.profile_setup_project_title
import io.github.vietdsk6.porta.generated.resources.profile_setup_relevant_experience
import io.github.vietdsk6.porta.generated.resources.profile_setup_residence
import io.github.vietdsk6.porta.generated.resources.profile_setup_role
import io.github.vietdsk6.porta.generated.resources.profile_setup_role_example
import io.github.vietdsk6.porta.generated.resources.profile_setup_short_summary
import io.github.vietdsk6.porta.generated.resources.profile_setup_team_activity
import io.github.vietdsk6.porta.generated.resources.profile_setup_your_gpa
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EssentialsSection(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
) {
    FormSection(
        title = stringResource(Res.string.profile_setup_essentials),
        description = stringResource(Res.string.profile_setup_essentials_hint),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            StudyLevelOption.entries.forEach { option ->
                FilterChip(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("profile-level-${option.storedValue}"),
                    selected = uiState.studyLevel == option,
                    onClick = { onUiEvent(ProfileSetupUiEvent.StudyLevelChanged(option)) },
                    label = { Text(option.localizedLabel()) },
                )
            }
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-field-of-study"),
            value = uiState.fieldOfStudy,
            onValueChange = { onUiEvent(ProfileSetupUiEvent.FieldOfStudyChanged(it)) },
            label = { Text(stringResource(Res.string.profile_setup_field_of_study)) },
            placeholder = { Text(stringResource(Res.string.profile_setup_field_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
    }
}

@Composable
internal fun AcademicRecordSection(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    var gpaTouched by rememberSaveable { mutableStateOf(false) }
    var ieltsTouched by rememberSaveable { mutableStateOf(false) }
    ProgressiveFormSection(
        title = stringResource(Res.string.profile_setup_academic),
        description = stringResource(Res.string.profile_setup_academic_hint),
        providedCount = uiState.academicProvidedCount,
        totalCount = 4,
        needsReview = uiState.gpaError != null || uiState.ieltsError != null,
        isExpanded = isExpanded,
        onToggle = onToggle,
        testTag = "profile-section-academic",
    ) {
        Text(stringResource(Res.string.profile_setup_gpa_scale), style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GpaScaleOption.entries.forEach { option ->
                FilterChip(
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    selected = uiState.gpaScale == option,
                    onClick = { onUiEvent(ProfileSetupUiEvent.GpaScaleChanged(option)) },
                    label = { Text(option.label) },
                )
            }
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-gpa"),
            value = uiState.gpa,
            onValueChange = {
                gpaTouched = true
                onUiEvent(ProfileSetupUiEvent.GpaChanged(it))
            },
            label = { Text(stringResource(Res.string.profile_setup_your_gpa)) },
            placeholder = { Text("3.6") },
            suffix = { Text("/ ${uiState.gpaScale.label}") },
            supportingText = { uiState.gpaError?.takeIf { gpaTouched }?.let { Text(profileSetupMessage(it, uiState.gpaScale).orEmpty()) } },
            isError = gpaTouched && uiState.gpaError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-ielts"),
            value = uiState.ielts,
            onValueChange = {
                ieltsTouched = true
                onUiEvent(ProfileSetupUiEvent.IeltsChanged(it))
            },
            label = { Text(stringResource(Res.string.profile_setup_ielts)) },
            placeholder = { Text(stringResource(Res.string.profile_setup_not_provided)) },
            supportingText = { uiState.ieltsError?.takeIf { ieltsTouched }?.let { Text(profileSetupMessage(it, uiState.gpaScale).orEmpty()) } },
            isError = ieltsTouched && uiState.ieltsError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        )
        BooleanFactField(
            label = stringResource(Res.string.profile_setup_currently_enrolled),
            value = uiState.isEnrolled,
            includeUnknown = true,
            onChanged = { onUiEvent(ProfileSetupUiEvent.EnrolledChanged(it)) },
        )
        BooleanFactField(
            label = stringResource(Res.string.profile_setup_degree_completed),
            value = uiState.isDegreeCompleted,
            includeUnknown = true,
            onChanged = { onUiEvent(ProfileSetupUiEvent.DegreeCompletedChanged(it)) },
        )
    }
}

@Composable
internal fun ExperienceEvidenceSection(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    var experienceTouched by rememberSaveable { mutableStateOf(false) }
    ProgressiveFormSection(
        title = stringResource(Res.string.profile_setup_experience_evidence),
        description = stringResource(Res.string.profile_setup_experience_hint),
        providedCount = uiState.experienceProvidedCount,
        totalCount = 3,
        needsReview = uiState.experienceMonthsError != null ||
            (
                uiState.communityProjectEvidenceReference.isNotBlank() &&
                    uiState.communityProjectTitle.isBlank() && uiState.communityProjectSummary.isBlank()
                ) ||
            (
                uiState.leadershipEvidenceReference.isNotBlank() &&
                    uiState.leadershipRole.isBlank() && uiState.leadershipTitle.isBlank()
                ),
        isExpanded = isExpanded,
        onToggle = onToggle,
        testTag = "profile-section-experience",
    ) {
        Text(
            text = stringResource(Res.string.profile_setup_experience),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-experience-months"),
            value = uiState.experienceMonths,
            onValueChange = {
                experienceTouched = true
                onUiEvent(ProfileSetupUiEvent.ExperienceMonthsChanged(it))
            },
            label = { Text(stringResource(Res.string.profile_setup_relevant_experience)) },
            placeholder = { Text(stringResource(Res.string.profile_setup_not_provided)) },
            suffix = { Text(stringResource(Res.string.profile_setup_months)) },
            supportingText = { uiState.experienceMonthsError?.takeIf { experienceTouched }?.let { Text(profileSetupMessage(it, uiState.gpaScale).orEmpty()) } },
            isError = experienceTouched && uiState.experienceMonthsError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        )
        HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.24f))
        GrowthEvidenceFields(
            heading = stringResource(Res.string.profile_setup_community_project),
            firstLabel = stringResource(Res.string.profile_setup_project_title),
            firstValue = uiState.communityProjectTitle,
            secondLabel = stringResource(Res.string.profile_setup_short_summary),
            secondValue = uiState.communityProjectSummary,
            firstPlaceholder = stringResource(Res.string.profile_setup_project_example),
            secondPlaceholder = stringResource(Res.string.profile_setup_project_summary_example),
            evidenceValue = uiState.communityProjectEvidenceReference,
            tagPrefix = "community-project",
            onFirstChanged = { onUiEvent(ProfileSetupUiEvent.CommunityProjectTitleChanged(it)) },
            onSecondChanged = { onUiEvent(ProfileSetupUiEvent.CommunityProjectSummaryChanged(it)) },
            onEvidenceChanged = { onUiEvent(ProfileSetupUiEvent.CommunityProjectEvidenceChanged(it)) },
        )
        HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.24f))
        GrowthEvidenceFields(
            heading = stringResource(Res.string.profile_setup_leadership),
            firstLabel = stringResource(Res.string.profile_setup_role),
            firstValue = uiState.leadershipRole,
            secondLabel = stringResource(Res.string.profile_setup_team_activity),
            secondValue = uiState.leadershipTitle,
            firstPlaceholder = stringResource(Res.string.profile_setup_role_example),
            secondPlaceholder = stringResource(Res.string.profile_setup_activity_example),
            evidenceValue = uiState.leadershipEvidenceReference,
            tagPrefix = "leadership",
            onFirstChanged = { onUiEvent(ProfileSetupUiEvent.LeadershipRoleChanged(it)) },
            onSecondChanged = { onUiEvent(ProfileSetupUiEvent.LeadershipTitleChanged(it)) },
            onEvidenceChanged = { onUiEvent(ProfileSetupUiEvent.LeadershipEvidenceChanged(it)) },
        )
    }
}

@Composable
internal fun ContextSection(
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    var ageTouched by rememberSaveable { mutableStateOf(false) }
    ProgressiveFormSection(
        title = stringResource(Res.string.profile_setup_context),
        description = stringResource(Res.string.profile_setup_context_hint),
        providedCount = uiState.contextProvidedCount,
        totalCount = 4,
        needsReview = uiState.nationalityError != null || uiState.residencyError != null ||
            uiState.ageError != null || uiState.dateOfBirthError != null,
        isExpanded = isExpanded,
        onToggle = onToggle,
        testTag = "profile-section-context",
    ) {
        CountrySelector(
            label = stringResource(Res.string.profile_setup_nationality),
            selectedCode = uiState.nationality,
            testTag = "profile-nationality",
            onSelected = { onUiEvent(ProfileSetupUiEvent.NationalityChanged(it)) },
        )
        CountrySelector(
            label = stringResource(Res.string.profile_setup_residence),
            selectedCode = uiState.residency,
            testTag = "profile-residency",
            onSelected = { onUiEvent(ProfileSetupUiEvent.ResidencyChanged(it)) },
        )
        BirthDateSelector(
            selectedDate = uiState.dateOfBirthValue,
            hasStoredValue = uiState.dateOfBirth.isNotBlank(),
            latestDate = uiState.validationDate,
            error = profileSetupMessage(uiState.dateOfBirthError, uiState.gpaScale),
            onSelected = { onUiEvent(ProfileSetupUiEvent.DateOfBirthChanged(it)) },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-age"),
            value = uiState.age,
            onValueChange = {
                ageTouched = true
                onUiEvent(ProfileSetupUiEvent.AgeChanged(it))
            },
            label = { Text(stringResource(Res.string.profile_setup_age_today)) },
            placeholder = { Text(stringResource(Res.string.profile_setup_not_provided)) },
            supportingText = {
                Text(
                    uiState.ageError?.takeIf { ageTouched }?.let { profileSetupMessage(it, uiState.gpaScale) }
                        ?: stringResource(Res.string.profile_setup_age_hint),
                )
            },
            isError = ageTouched && uiState.ageError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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
    firstPlaceholder: String,
    secondPlaceholder: String,
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
            placeholder = { Text(firstPlaceholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-$tagPrefix-secondary"),
            value = secondValue,
            onValueChange = onSecondChanged,
            label = { Text(secondLabel) },
            placeholder = { Text(secondPlaceholder) },
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-$tagPrefix-evidence"),
            value = evidenceValue,
            onValueChange = onEvidenceChanged,
            label = { Text(stringResource(Res.string.profile_setup_evidence_reference)) },
            placeholder = { Text(stringResource(Res.string.profile_setup_evidence_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
    }
}
