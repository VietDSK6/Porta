package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun ProfileSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel,
    onCompleted: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenDataPrivacy: () -> Unit = {},
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
        onOpenDataPrivacy = onOpenDataPrivacy,
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
    onOpenDataPrivacy: () -> Unit = {},
    requestedFactKey: FactKey? = null,
    requestContext: String? = null,
) {
    var expandedSectionName by rememberSaveable(requestedFactKey?.name) {
        mutableStateOf(requestedFactKey?.setupSection()?.name)
    }
    var hasEdited by rememberSaveable { mutableStateOf(false) }
    val onEditEvent: (ProfileSetupUiEvent) -> Unit = { event ->
        hasEdited = true
        onUiEvent(event)
    }

    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = true,
        title = "Edit profile",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .testTag("profile-setup-form"),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
            ) {
                ProfileSetupHeader()
                requestedFactKey?.let { factKey ->
                    RequestedFactNotice(
                        factKey = factKey,
                        requestContext = requestContext,
                    )
                }
                EssentialsSection(uiState = uiState, onUiEvent = onEditEvent)
                AdditionalFactsIntro()
                AcademicRecordSection(
                    uiState = uiState,
                    onUiEvent = onEditEvent,
                    isExpanded = expandedSectionName == ProfileSetupSection.ACADEMIC.name,
                    onToggle = {
                        expandedSectionName = ProfileSetupSection.ACADEMIC.name
                            .takeUnless { it == expandedSectionName }
                    },
                )
                ExperienceEvidenceSection(
                    uiState = uiState,
                    onUiEvent = onEditEvent,
                    isExpanded = expandedSectionName == ProfileSetupSection.EXPERIENCE.name,
                    onToggle = {
                        expandedSectionName = ProfileSetupSection.EXPERIENCE.name
                            .takeUnless { it == expandedSectionName }
                    },
                )
                ContextSection(
                    uiState = uiState,
                    onUiEvent = onEditEvent,
                    isExpanded = expandedSectionName == ProfileSetupSection.CONTEXT.name,
                    onToggle = {
                        expandedSectionName = ProfileSetupSection.CONTEXT.name
                            .takeUnless { it == expandedSectionName }
                    },
                )
                LocalDataNotice(onOpenDataPrivacy)

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
                        color = if (hasEdited) AppTheme.colors.status.error else AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
            }
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
            text = "More about you",
            style = AppTheme.typography.h5,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Optional details. Leave anything you do not know blank.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
private fun ProfileSetupHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Update what you know today",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyLarge,
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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
    var gpaTouched by rememberSaveable { mutableStateOf(false) }
    var ieltsTouched by rememberSaveable { mutableStateOf(false) }
    ProgressiveFormSection(
        title = "Academic record",
        description = "Keep the original GPA scale. Porta never silently converts it.",
        providedCount = uiState.academicProvidedCount,
        totalCount = 4,
        needsReview = uiState.gpaError != null || uiState.ieltsError != null,
        isExpanded = isExpanded,
        onToggle = onToggle,
        testTag = "profile-section-academic",
    ) {
        Text("GPA scale", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.primary)
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
            label = { Text("Your GPA") },
            placeholder = { Text("3.6") },
            suffix = { Text("/ ${uiState.gpaScale.label}") },
            supportingText = { uiState.gpaError?.takeIf { gpaTouched }?.let { Text(it) } },
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
            label = { Text("IELTS overall band") },
            placeholder = { Text("Not provided") },
            supportingText = { uiState.ieltsError?.takeIf { ieltsTouched }?.let { Text(it) } },
            isError = ieltsTouched && uiState.ieltsError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
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
    var experienceTouched by rememberSaveable { mutableStateOf(false) }
    ProgressiveFormSection(
        title = "Experience and evidence",
        description = "Growth evidence adds context. It never passes a published check automatically.",
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
            text = "Experience",
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
            label = { Text("Relevant experience") },
            placeholder = { Text("Not provided") },
            suffix = { Text("months") },
            supportingText = { uiState.experienceMonthsError?.takeIf { experienceTouched }?.let { Text(it) } },
            isError = experienceTouched && uiState.experienceMonthsError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        )
        HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.24f))
        GrowthEvidenceFields(
            heading = "Community project",
            firstLabel = "Project title",
            firstValue = uiState.communityProjectTitle,
            secondLabel = "Short summary",
            secondValue = uiState.communityProjectSummary,
            firstPlaceholder = "Peer coding club",
            secondPlaceholder = "Ran a Kotlin workshop for first-year students",
            evidenceValue = uiState.communityProjectEvidenceReference,
            tagPrefix = "community-project",
            onFirstChanged = { onUiEvent(ProfileSetupUiEvent.CommunityProjectTitleChanged(it)) },
            onSecondChanged = { onUiEvent(ProfileSetupUiEvent.CommunityProjectSummaryChanged(it)) },
            onEvidenceChanged = { onUiEvent(ProfileSetupUiEvent.CommunityProjectEvidenceChanged(it)) },
        )
        HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.24f))
        GrowthEvidenceFields(
            heading = "Leadership",
            firstLabel = "Role",
            firstValue = uiState.leadershipRole,
            secondLabel = "Team or activity",
            secondValue = uiState.leadershipTitle,
            firstPlaceholder = "Student mentor",
            secondPlaceholder = "Faculty peer network",
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
    var ageTouched by rememberSaveable { mutableStateOf(false) }
    ProgressiveFormSection(
        title = "Context",
        description = "Used only when an official rule depends on it; never suggested as something to change.",
        providedCount = uiState.contextProvidedCount,
        totalCount = 4,
        needsReview = uiState.nationalityError != null || uiState.residencyError != null ||
            uiState.ageError != null || uiState.dateOfBirthError != null,
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
        BirthDateSelector(
            selectedDate = uiState.dateOfBirthValue,
            hasStoredValue = uiState.dateOfBirth.isNotBlank(),
            latestDate = uiState.validationDate,
            error = uiState.dateOfBirthError,
            onSelected = { onUiEvent(ProfileSetupUiEvent.DateOfBirthChanged(it)) },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("profile-age"),
            value = uiState.age,
            onValueChange = {
                ageTouched = true
                onUiEvent(ProfileSetupUiEvent.AgeChanged(it))
            },
            label = { Text("Age today (self-reported)") },
            placeholder = { Text("Not provided") },
            supportingText = {
                Text(uiState.ageError?.takeIf { ageTouched } ?: "Optional. Review this value as you get older.")
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
            label = { Text("Evidence reference (optional)") },
            placeholder = { Text("URL, document name, or portfolio reference") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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
    val selectedCountry = countryForStoredValue(selectedCode)
    OutlinedButton(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp).testTag(testTag),
        onClick = { expanded = true },
    ) {
        Text(
            text = selectedCountry?.let { "$label: ${it.name}" } ?: "$label: Not provided",
            modifier = Modifier.fillMaxWidth(),
            color = if (selectedCountry == null) AppTheme.colors.text.secondary else AppTheme.colors.text.primary,
        )
    }
    if (expanded) {
        CountryPickerDialog(
            label = label,
            selectedCode = selectedCountry?.alpha2,
            onDismiss = { expanded = false },
            onSelected = {
                onSelected(it)
                expanded = false
            },
        )
    }
}

@Composable
private fun CountryPickerDialog(
    label: String,
    selectedCode: String?,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    var query by rememberSaveable(label) { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    val matches = remember(query) {
        val normalized = query.trim()
        ISO_COUNTRIES.filter { country ->
            normalized.isBlank() ||
                country.name.contains(normalized, ignoreCase = true) ||
                country.alpha2.contains(normalized, ignoreCase = true)
        }
    }
    Dialog(onDismissRequest = onDismiss) {
        BoxWithConstraints(Modifier.fillMaxWidth().imePadding()) {
            val dialogHeight = maxHeight.coerceAtMost(560.dp)
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).heightIn(max = dialogHeight),
                shape = RoundedCornerShape(20.dp),
                color = AppTheme.colors.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.heightIn(max = dialogHeight).padding(AppTheme.spacing.defaultSpacing),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Choose $label",
                            modifier = Modifier.weight(1f),
                            style = AppTheme.typography.h6,
                            color = AppTheme.colors.text.primary,
                        )
                        TextButton(onClick = onDismiss, modifier = Modifier.testTag("country-cancel")) {
                            Text("Cancel")
                        }
                    }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().focusRequester(searchFocus).testTag("country-search"),
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search by name or code") },
                        placeholder = { Text("Vietnam or VN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    )
                    Text(
                        text = "${matches.size} results",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).testTag("country-results"),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (query.isBlank()) {
                            item {
                                CountryPickerRow(
                                    text = "Not provided",
                                    selected = selectedCode == null,
                                    onClick = { onSelected("") },
                                )
                            }
                        }
                        items(matches, key = IsoCountry::alpha2) { country ->
                            CountryPickerRow(
                                text = country.displayName,
                                selected = selectedCode == country.alpha2,
                                onClick = { onSelected(country.alpha2) },
                                testTag = "country-${country.alpha2.lowercase()}",
                            )
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { searchFocus.requestFocus() }
}

@Composable
private fun CountryPickerRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String = "",
) {
    Text(
        text = if (selected) "$text · Selected" else text,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.colors.text.primary,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateSelector(
    selectedDate: LocalDate?,
    hasStoredValue: Boolean,
    latestDate: LocalDate?,
    error: String?,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Date of birth", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 52.dp).testTag("profile-date-of-birth"),
                onClick = { expanded = true },
            ) {
                Text(selectedDate?.let(::displayBirthDate) ?: "Choose a date", modifier = Modifier.fillMaxWidth())
            }
            if (hasStoredValue) {
                TextButton(onClick = { onSelected("") }, modifier = Modifier.testTag("profile-date-clear")) {
                    Text("Clear")
                }
            }
        }
        Text(
            text = error ?: "Optional. Used for age checks on a named date; separate from age today.",
            color = if (error == null) AppTheme.colors.text.secondary else AppTheme.colors.status.error,
            style = AppTheme.typography.bodySmall,
        )
    }
    if (expanded) {
        val today = latestDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val initialDate = selectedDate ?: LocalDate(today.year - 20, 1, 1)
        val selectableBirthDates = remember(today) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= today.toUtcMillis()

                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.toUtcMillis(),
            initialDisplayedMonthMillis = initialDate.toUtcMillis(),
            selectableDates = selectableBirthDates,
        )
        DatePickerDialog(
            onDismissRequest = { expanded = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis?.let { it <= today.toUtcMillis() } == true,
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onSelected(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date.toString())
                        }
                        expanded = false
                    },
                    modifier = Modifier.testTag("profile-date-confirm"),
                ) { Text("Use date") }
            },
            dismissButton = {
                TextButton(onClick = { expanded = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun LocalDate.toUtcMillis(): Long = toEpochDays() * 86_400_000L

private fun displayBirthDate(date: LocalDate): String = "${date.day} ${date.month.name.lowercase().replaceFirstChar(Char::uppercase)} ${date.year}"

@Composable
private fun BooleanFactField(
    label: String,
    value: Boolean?,
    includeUnknown: Boolean,
    onChanged: (Boolean?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.primary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilterChip(
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                selected = value == true,
                onClick = { onChanged(true) },
                label = { Text("Yes") },
            )
            FilterChip(
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                selected = value == false,
                onClick = { onChanged(false) },
                label = { Text("No") },
            )
            if (includeUnknown) {
                FilterChip(
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
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
    providedCount: Int,
    totalCount: Int,
    needsReview: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit,
) {
    val status = providedCount.providedStatus(totalCount, needsReview)
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
                    .defaultMinSize(minHeight = 64.dp)
                    .semantics {
                        stateDescription = if (isExpanded) "Expanded" else "Collapsed, $status"
                    }
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
                        needsReview -> "Fix"
                        providedCount == 0 -> "Add"
                        else -> "Review"
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
private fun LocalDataNotice(onOpenDataPrivacy: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Saved on this device.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
        TextButton(
            onClick = onOpenDataPrivacy,
            modifier = Modifier.testTag("profile-setup-data-privacy"),
        ) {
            Text("Data & privacy")
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

private fun Int.providedStatus(total: Int, needsReview: Boolean = false): String = if (needsReview) {
    "$this of $total provided · needs review"
} else {
    "$this of $total provided · optional"
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

@Preview(widthDp = 320, heightDp = 640, fontScale = 2f)
@Composable
private fun ProfileSetupAccessibilityPreview() {
    AppTheme {
        ProfileSetupScreen(
            uiState = ProfileSetupUiState(
                studyLevel = StudyLevelOption.UNDERGRADUATE,
                fieldOfStudy = "Computer Science",
            ),
            onUiEvent = {},
            onNavigateBack = {},
        )
    }
}
