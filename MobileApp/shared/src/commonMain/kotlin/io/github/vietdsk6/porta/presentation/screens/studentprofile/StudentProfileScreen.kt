package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ButtonStyle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey

@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: StudentProfileViewModel,
    onEditProfile: () -> Unit,
    onOpenPackInspector: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StudentProfileScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onEditProfile = onEditProfile,
        onOpenPackInspector = onOpenPackInspector,
    )
}

@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
    onEditProfile: () -> Unit,
    onOpenPackInspector: () -> Unit,
) {
    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = true,
        title = "Profile",
        includeBottomInsets = false,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            when {
                uiState.isLoading -> LoadingProfile()

                !uiState.hasRealProfile -> EmptyProfile(
                    onEditProfile = onEditProfile,
                    onOpenPackInspector = onOpenPackInspector,
                )

                else -> ProfileContent(
                    uiState = uiState,
                    onUiEvent = onUiEvent,
                    onEditProfile = onEditProfile,
                    onOpenPackInspector = onOpenPackInspector,
                )
            }

            uiState.message?.let { message ->
                MessageNotice(
                    message = message,
                    onDismiss = { onUiEvent(StudentProfileUiEvent.MessageHandled) },
                )
            }
        }
    }

    uiState.factPendingDelete?.let { fact ->
        AlertDialog(
            onDismissRequest = { onUiEvent(StudentProfileUiEvent.DeleteFactCancelled) },
            title = { Text("Remove ${fact.label}?") },
            text = {
                Text("Rules that use this fact will become unknown until you add it again.")
            },
            confirmButton = {
                TextButton(onClick = { onUiEvent(StudentProfileUiEvent.DeleteFactConfirmed) }) {
                    Text("Remove fact")
                }
            },
            dismissButton = {
                TextButton(onClick = { onUiEvent(StudentProfileUiEvent.DeleteFactCancelled) }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (uiState.isClearConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { onUiEvent(StudentProfileUiEvent.ClearProfileCancelled) },
            title = { Text("Delete local Porta data?") },
            text = {
                Text(
                    "This removes every profile fact and local plan on this device. " +
                        "Opportunity packs stay available.",
                )
            },
            confirmButton = {
                TextButton(onClick = { onUiEvent(StudentProfileUiEvent.ClearProfileConfirmed) }) {
                    Text("Delete profile and plans")
                }
            },
            dismissButton = {
                TextButton(onClick = { onUiEvent(StudentProfileUiEvent.ClearProfileCancelled) }) {
                    Text("Keep data")
                }
            },
        )
    }
}

@Composable
private fun LoadingProfile() {
    Column(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyProfile(
    onEditProfile: () -> Unit,
    onOpenPackInspector: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-empty"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Text(
                text = "NO REAL PROFILE YET",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
            )
            Text(
                text = "Build your source of truth",
                style = AppTheme.typography.h4,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "Add a few current facts. Porta evaluates only published hard rules and keeps the data on this device.",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                text = "Build my profile",
                modifier = Modifier.fillMaxWidth().testTag("student-profile-build"),
                onClick = onEditProfile,
            )
            AppButton(
                text = "Inspect opportunity pack",
                modifier = Modifier.fillMaxWidth().testTag("student-profile-pack-inspector"),
                style = ButtonStyle.ALTERNATIVE,
                onClick = onOpenPackInspector,
            )
        }
    }
}

@Composable
private fun ProfileContent(
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
    onEditProfile: () -> Unit,
    onOpenPackInspector: () -> Unit,
) {
    ProfileSummary(
        factCount = uiState.factCount,
        isWorking = uiState.isWorking,
        onEditProfile = onEditProfile,
    )

    if (uiState.isSampleCatalog) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.alternative,
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "Used-by counts currently refer to Porta's synthetic demo pack, not verified real opportunities.",
                modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
            )
        }
    }

    uiState.sections.forEach { section ->
        FactSection(
            section = section,
            expandedFactKey = uiState.expandedFactKey,
            onUiEvent = onUiEvent,
        )
    }

    DataControls(
        isWorking = uiState.isWorking,
        onOpenPackInspector = onOpenPackInspector,
        onExport = { onUiEvent(StudentProfileUiEvent.ExportMetadata) },
        onClear = { onUiEvent(StudentProfileUiEvent.ClearProfileRequested) },
    )
}

@Composable
private fun ProfileSummary(
    factCount: Int,
    isWorking: Boolean,
    onEditProfile: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-summary"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Text(
                text = "LOCAL SOURCE OF TRUTH",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
            )
            Text(
                text = "$factCount facts saved on this device",
                style = AppTheme.typography.h4,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "What-if previews never change these values. Only an explicit edit updates your real profile.",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                text = "Edit profile facts",
                modifier = Modifier.fillMaxWidth().testTag("student-profile-edit"),
                style = ButtonStyle.ALTERNATIVE,
                enabled = !isWorking,
                onClick = onEditProfile,
            )
        }
    }
}

@Composable
private fun FactSection(
    section: ProfileFactSectionUiState,
    expandedFactKey: FactKey?,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.verticalListItemSpacingSmall)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall)) {
            Text(
                text = section.title,
                style = AppTheme.typography.h5,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = section.description,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing)) {
            section.facts.forEach { fact ->
                FactCard(
                    fact = fact,
                    expanded = fact.key == expandedFactKey,
                    onToggle = { onUiEvent(StudentProfileUiEvent.ToggleDependencies(fact.key)) },
                    onDelete = { onUiEvent(StudentProfileUiEvent.DeleteFactRequested(fact)) },
                )
            }
        }
    }
}

@Composable
private fun FactCard(
    fact: ProfileFactUiState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student-profile-fact-${fact.key.name.lowercase()}")
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = fact.label,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.secondary,
                    )
                    Text(
                        text = fact.value,
                        style = AppTheme.typography.h6,
                        color = AppTheme.colors.text.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = if (expanded) "Hide" else "Inspect",
                    color = AppTheme.colors.primary,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = "${fact.provenanceLabel} · ${fact.actionabilityLabel}",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            fact.evidenceLabel?.let { evidence ->
                Text(
                    text = "Evidence: $evidence",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Text(
                text = fact.usedByLabel,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )

            if (expanded) {
                DependencyDetails(fact = fact, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun DependencyDetails(
    fact: ProfileFactUiState,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.background,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacingSmall),
        ) {
            Text(
                text = "RULE DEPENDENCIES",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
            )
            if (fact.dependentDoorTitles.isEmpty()) {
                Text(
                    text = "No encoded rule in the current pack reads this fact.",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.text.primary,
                )
            } else {
                fact.dependentDoorTitles.forEach { title ->
                    Text(
                        text = "• $title",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.primary,
                    )
                }
            }
            TextButton(
                modifier = Modifier.testTag("student-profile-delete-${fact.key.name.lowercase()}"),
                onClick = onDelete,
            ) {
                Text("Remove this fact")
            }
        }
    }
}

@Composable
private fun DataControls(
    isWorking: Boolean,
    onOpenPackInspector: () -> Unit,
    onExport: () -> Unit,
    onClear: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-data-controls"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.sectionSpacing),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
        ) {
            Text(
                text = "Data controls",
                style = AppTheme.typography.h6,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Export includes values and evidence metadata, never evidence binary files. " +
                    "Delete clears the profile and plans from this device.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                text = "Inspect opportunity pack",
                modifier = Modifier.fillMaxWidth().testTag("student-profile-pack-inspector"),
                style = ButtonStyle.ALTERNATIVE,
                enabled = !isWorking,
                onClick = onOpenPackInspector,
            )
            AppButton(
                text = "Export profile metadata",
                modifier = Modifier.fillMaxWidth().testTag("student-profile-export"),
                style = ButtonStyle.ALTERNATIVE,
                enabled = !isWorking,
                onClick = onExport,
            )
            AppButton(
                text = "Delete local profile and plans",
                modifier = Modifier.fillMaxWidth().testTag("student-profile-clear"),
                style = ButtonStyle.TEXT,
                enabled = !isWorking,
                onClick = onClear,
            )
        }
    }
}

@Composable
private fun MessageNotice(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
            )
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Preview
@Composable
private fun StudentProfileScreenPreview() {
    AppTheme {
        StudentProfileScreen(
            uiState = previewState,
            onUiEvent = {},
            onEditProfile = {},
            onOpenPackInspector = {},
        )
    }
}

@Preview
@Composable
private fun StudentProfileCollapsedScreenPreview() {
    AppTheme {
        StudentProfileScreen(
            uiState = previewState.copy(
                isSampleCatalog = false,
                expandedFactKey = null,
            ),
            onUiEvent = {},
            onEditProfile = {},
            onOpenPackInspector = {},
        )
    }
}

private val previewState = StudentProfileUiState(
    isLoading = false,
    hasRealProfile = true,
    factCount = 4,
    isSampleCatalog = true,
    expandedFactKey = FactKey.IELTS_SCORE,
    sections = listOf(
        ProfileFactSectionUiState(
            title = "Academic",
            description = "Study status and measurable academic facts.",
            facts = listOf(
                ProfileFactUiState(
                    key = FactKey.GPA,
                    label = "GPA",
                    value = "3.6 4.0 scale",
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
                    dependentDoorTitles = listOf(
                        "Aurora Scholars",
                        "Forge Student Lab",
                        "Horizon Exchange",
                        "Spark Fellowship",
                    ),
                ),
            ),
        ),
        ProfileFactSectionUiState(
            title = "Location and identity context",
            description = "Context can explain a rule, but Porta never recommends changing it.",
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
            ),
        ),
    ),
)
