package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vinceglb.filekit.mimeType

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
