package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey

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
