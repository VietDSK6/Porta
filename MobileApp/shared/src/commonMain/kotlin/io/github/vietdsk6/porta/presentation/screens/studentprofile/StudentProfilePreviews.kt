package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.presentation.locale.LocalAppLocale

@Preview(widthDp = 390, heightDp = 844)
@Composable
internal fun StudentProfileScreenPreview() {
    AppTheme {
        StudentProfileScreen(
            uiState = previewState,
            onUiEvent = {},
            onEditProfile = {},
            onOpenPackInspector = {},
            onOpenDataPrivacy = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
internal fun StudentProfileDemoScreenPreview() {
    AppTheme {
        StudentProfileScreen(
            uiState = previewState.copy(isDemoProfile = true),
            onUiEvent = {},
            onEditProfile = {},
            onOpenPackInspector = {},
            onOpenDataPrivacy = {},
        )
    }
}

@Preview(name = "Demo profile Vietnamese", widthDp = 390, heightDp = 844, locale = "vi")
@Composable
internal fun StudentProfileDemoVietnamesePreview() {
    CompositionLocalProvider(LocalAppLocale provides "vi") {
        AppTheme {
            StudentProfileScreen(
                uiState = previewState.copy(isDemoProfile = true),
                onUiEvent = {},
                onEditProfile = {},
                onOpenPackInspector = {},
                onOpenDataPrivacy = {},
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
internal fun StudentProfileExpandedScreenPreview() {
    AppTheme {
        StudentProfileScreen(
            uiState = previewState.copy(expandedFactKey = FactKey.IELTS_SCORE),
            onUiEvent = {},
            onEditProfile = {},
            onOpenPackInspector = {},
            onOpenDataPrivacy = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
internal fun StudentProfileDataPrivacyEntryPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.background,
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                DataPrivacyEntry(isWorking = false, onClick = {})
            }
        }
    }
}

internal val previewState = StudentProfileUiState(
    isLoading = false,
    hasRealProfile = true,
    aiDraftSettingsAvailable = true,
    factCount = 9,
    catalogOpportunityCount = 15,
    dependentOpportunityCount = 9,
    isSampleCatalog = false,
    expandedFactKey = null,
    sections = listOf(
        ProfileFactSectionUiState(
            title = "Academic",
            description = "Study status and measurable academic facts.",
            facts = listOf(
                ProfileFactUiState(
                    key = FactKey.STUDY_LEVEL,
                    label = "Study level",
                    value = "undergraduate",
                    provenanceLabel = "Self-reported",
                    actionabilityLabel = "Changes with time",
                    evidenceLabel = null,
                    dependentDoorTitles = emptyList(),
                ),
                ProfileFactUiState(
                    key = FactKey.FIELD_OF_STUDY,
                    label = "Field of study",
                    value = "Computer Science",
                    provenanceLabel = "Self-reported",
                    actionabilityLabel = "Context only",
                    evidenceLabel = null,
                    dependentDoorTitles = emptyList(),
                ),
                ProfileFactUiState(
                    key = FactKey.GPA,
                    label = "GPA",
                    value = "3.6 / 4.0",
                    provenanceLabel = "Self-reported",
                    actionabilityLabel = "Can change",
                    evidenceLabel = null,
                    dependentDoorTitles = emptyList(),
                ),
                ProfileFactUiState(
                    key = FactKey.IELTS_SCORE,
                    label = "IELTS score",
                    value = "6.5 band",
                    provenanceLabel = "Evidence-backed",
                    actionabilityLabel = "Can change",
                    evidenceLabel = "Official score report",
                    dependentDoorTitles = emptyList(),
                    evidenceAttachmentName = "ielts-score-report.pdf",
                    evidenceAttachmentDetail = "application/pdf · 448 KB",
                    canOpenEvidenceAttachment = true,
                ),
                ProfileFactUiState(
                    key = FactKey.ENROLLED,
                    label = "Currently enrolled",
                    value = "Yes",
                    provenanceLabel = "Self-reported",
                    actionabilityLabel = "Changes with time",
                    evidenceLabel = null,
                    dependentDoorTitles = listOf(
                        "Kotlin Student Coding Competition",
                        "OECD Internship Programme",
                        "UNICEF Internship Programme",
                        "CERN openlab Summer Student Programme",
                    ),
                ),
                ProfileFactUiState(
                    key = FactKey.DEGREE_COMPLETED,
                    label = "Degree completed",
                    value = "No",
                    provenanceLabel = "Self-reported",
                    actionabilityLabel = "Changes with time",
                    evidenceLabel = null,
                    dependentDoorTitles = listOf(
                        "Erasmus Mundus Joint Masters",
                        "Chevening Scholarship",
                        "DAAD EPOS",
                        "Joint Japan/World Bank Graduate Scholarship",
                    ),
                ),
            ),
        ),
        ProfileFactSectionUiState(
            title = "Identity context",
            description = "Context explains rules; Porta never recommends changing it.",
            facts = listOf(
                ProfileFactUiState(
                    key = FactKey.NATIONALITY,
                    label = "Nationality",
                    value = "VN",
                    provenanceLabel = "Self-reported",
                    actionabilityLabel = "Context only",
                    evidenceLabel = null,
                    dependentDoorTitles = emptyList(),
                ),
                ProfileFactUiState(
                    key = FactKey.AGE,
                    label = "Age",
                    value = "20 years",
                    provenanceLabel = "Self-reported",
                    actionabilityLabel = "Not an action target",
                    evidenceLabel = null,
                    dependentDoorTitles = listOf(
                        "Kotlin Student Coding Competition",
                        "UNICEF Internship Programme",
                        "Google Summer of Code",
                    ),
                ),
            ),
        ),
        ProfileFactSectionUiState(
            title = "Experience and evidence",
            description = "Experience and documented projects.",
            facts = listOf(
                ProfileFactUiState(
                    key = FactKey.EXPERIENCE_MONTHS,
                    label = "Experience",
                    value = "6 months",
                    provenanceLabel = "Self-reported",
                    actionabilityLabel = "Changes with time",
                    evidenceLabel = null,
                    dependentDoorTitles = listOf("DAAD EPOS", "Joint Japan/World Bank Graduate Scholarship"),
                ),
            ),
        ),
    ),
)
