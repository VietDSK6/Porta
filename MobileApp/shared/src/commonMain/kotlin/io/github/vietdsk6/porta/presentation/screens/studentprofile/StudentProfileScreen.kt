package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        title = "Your academic profile",
        includeBottomInsets = false,
        toolbarExtraContent = {
            if (uiState.hasRealProfile) {
                TextButton(
                    modifier = Modifier.testTag("student-profile-edit"),
                    enabled = !uiState.isWorking,
                    onClick = onEditProfile,
                ) {
                    Text(
                        text = "Edit",
                        color = AppTheme.colors.primary.copy(alpha = if (uiState.isWorking) 0.5f else 1f),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
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
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileAvatar(monogram = "+")
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "START YOUR PROFILE",
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = "Make Porta yours",
                        style = AppTheme.typography.h4,
                        color = AppTheme.colors.text.primary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Text(
                text = "Add the academic facts that shape your opportunities. They stay on this device and remain under your control.",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                text = "Build my profile",
                modifier = Modifier.fillMaxWidth().testTag("student-profile-build"),
                onClick = onEditProfile,
            )
            AppButton(
                text = "Explore the opportunity pack",
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
    onOpenPackInspector: () -> Unit,
) {
    ProfileIdentity(uiState = uiState)

    if (uiState.isSampleCatalog) {
        DemoPackNotice()
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
private fun ProfileIdentity(uiState: StudentProfileUiState) {
    val facts = uiState.sections.flatMap(ProfileFactSectionUiState::facts)
    val fieldOfStudy = facts.valueOf(FactKey.FIELD_OF_STUDY)
    val studyLevel = facts.valueOf(FactKey.STUDY_LEVEL)?.sentenceCase()
    val nationality = facts.valueOf(FactKey.NATIONALITY)
    val identityLine = listOfNotNull(studyLevel, nationality).joinToString(" · ")
    val usedByDoorCount = facts.flatMap(ProfileFactUiState::dependentDoorTitles).distinct().size
    val evidenceCount = facts.count { it.evidenceLabel != null }

    Surface(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-summary"),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileAvatar(monogram = fieldOfStudy.initials())
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "ACADEMIC PROFILE",
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = fieldOfStudy?.let { "$it student" } ?: "Your student profile",
                        style = AppTheme.typography.h5,
                        color = AppTheme.colors.text.primary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = identityLine.ifEmpty { "Built from your current academic facts" },
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.secondary,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfilePill("${uiState.factCount} facts")
                ProfilePill("$usedByDoorCount doors")
                if (evidenceCount > 0) ProfilePill("$evidenceCount documented")
            }

            Text(
                text = "Private by design · stored only on this device",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
private fun ProfileAvatar(monogram: String) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monogram,
            style = AppTheme.typography.h4,
            color = AppTheme.colors.onPrimary,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun ProfilePill(label: String) {
    Surface(
        color = AppTheme.colors.surfaceContainer.copy(alpha = 0.86f),
        shape = CircleShape,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DemoPackNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(color = AppTheme.colors.alternative, shape = CircleShape) {
                Text(
                    text = "DEMO PACK",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = AppTheme.typography.bodyExtraSmall,
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                )
            }
            Text(
                text = "Door usage below comes from Porta's synthetic opportunity pack.",
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
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
            Text(
                text = section.facts.size.toString().padStart(2, '0'),
                style = AppTheme.typography.h6,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
            border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
        ) {
            Column {
                section.facts.forEachIndexed { index, fact ->
                    FactRow(
                        fact = fact,
                        expanded = fact.key == expandedFactKey,
                        onToggle = { onUiEvent(StudentProfileUiEvent.ToggleDependencies(fact.key)) },
                        onDelete = { onUiEvent(StudentProfileUiEvent.DeleteFactRequested(fact)) },
                    )
                    if (index != section.facts.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = AppTheme.colors.outline.copy(alpha = 0.24f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FactRow(
    fact: ProfileFactUiState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student-profile-fact-${fact.key.name.lowercase()}")
            .clickable(role = Role.Button, onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = fact.label,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
                Text(
                    text = fact.value,
                    style = AppTheme.typography.h6,
                    color = AppTheme.colors.text.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                fact.detail?.let { detail ->
                    Text(
                        text = detail,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.secondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = if (expanded) "Close" else "Details",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${fact.provenanceLabel} · ${fact.actionabilityLabel}",
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            DoorCountBadge(fact.dependentDoorTitles.size)
        }

        fact.evidenceLabel?.let { evidence ->
            Text(
                text = "Evidence · $evidence",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (fact.isGrowthEvidence) {
            Text(
                text = "Growth evidence · never used to auto-pass a hard requirement",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }

        if (expanded) {
            DependencyDetails(fact = fact, onDelete = onDelete)
        }
    }
}

@Composable
private fun DoorCountBadge(count: Int) {
    val hasDependencies = count > 0
    Surface(
        color = if (hasDependencies) AppTheme.colors.alternative else AppTheme.colors.background,
        shape = CircleShape,
    ) {
        Text(
            text = when (count) {
                0 -> "No doors"
                1 -> "1 door"
                else -> "$count doors"
            },
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = AppTheme.typography.bodyExtraSmall,
            color = if (hasDependencies) AppTheme.colors.primary else AppTheme.colors.text.secondary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DependencyDetails(
    fact: ProfileFactUiState,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (fact.dependentDoorTitles.isEmpty()) "NO RULE DEPENDENCIES" else "USED BY THESE DOORS",
                style = AppTheme.typography.bodyExtraSmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.7.sp,
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Privacy & data",
                style = AppTheme.typography.h5,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Review what uses your profile, take a copy, or remove it.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().testTag("student-profile-data-controls"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
            border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.35f)),
        ) {
            Column {
                DataActionRow(
                    title = "Opportunity pack",
                    supportingText = "See which encoded rules read your facts.",
                    actionLabel = "Inspect",
                    modifier = Modifier.testTag("student-profile-pack-inspector"),
                    enabled = !isWorking,
                    onClick = onOpenPackInspector,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = AppTheme.colors.outline.copy(alpha = 0.24f),
                )
                DataActionRow(
                    title = "Profile metadata",
                    supportingText = "Export values and evidence metadata as JSON.",
                    actionLabel = "Export",
                    modifier = Modifier.testTag("student-profile-export"),
                    enabled = !isWorking,
                    onClick = onExport,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = AppTheme.colors.outline.copy(alpha = 0.24f),
                )
                DataActionRow(
                    title = "Local profile and plans",
                    supportingText = "Permanently remove them from this device.",
                    actionLabel = "Delete",
                    modifier = Modifier.testTag("student-profile-clear"),
                    enabled = !isWorking,
                    actionColor = MaterialTheme.colorScheme.error,
                    onClick = onClear,
                )
            }
        }
    }
}

@Composable
private fun DataActionRow(
    title: String,
    supportingText: String,
    actionLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    actionColor: Color = AppTheme.colors.primary,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.primary.copy(alpha = if (enabled) 1f else 0.5f),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = supportingText,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary.copy(alpha = if (enabled) 1f else 0.5f),
            )
        }
        Text(
            text = actionLabel,
            style = AppTheme.typography.bodyMedium,
            color = actionColor.copy(alpha = if (enabled) 1f else 0.5f),
            fontWeight = FontWeight.Bold,
        )
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

private fun List<ProfileFactUiState>.valueOf(key: FactKey): String? = firstOrNull { it.key == key }
    ?.value
    ?.takeIf(String::isNotBlank)

private fun String?.initials(): String {
    if (isNullOrBlank()) return "P"
    return trim()
        .split(Regex("\\s+"))
        .take(2)
        .mapNotNull(String::firstOrNull)
        .joinToString(separator = "")
        .uppercase()
}

private fun String.sentenceCase(): String = lowercase().replaceFirstChar(Char::uppercaseChar)

@Preview(widthDp = 390, heightDp = 844)
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

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun StudentProfileExpandedScreenPreview() {
    AppTheme {
        StudentProfileScreen(
            uiState = previewState.copy(expandedFactKey = FactKey.IELTS_SCORE),
            onUiEvent = {},
            onEditProfile = {},
            onOpenPackInspector = {},
        )
    }
}

private val previewState = StudentProfileUiState(
    isLoading = false,
    hasRealProfile = true,
    factCount = 10,
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
            title = "Experience and availability",
            description = "Experience, projects and time commitments.",
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
                ProfileFactUiState(
                    key = FactKey.AVAILABLE_FULL_PERIOD,
                    label = "Available for full period",
                    value = "No",
                    provenanceLabel = "Self-reported",
                    actionabilityLabel = "Can change",
                    evidenceLabel = null,
                    dependentDoorTitles = listOf(
                        "OECD Internship Programme",
                        "World Bank Treasury Summer Internship",
                        "ESA Student Internships",
                        "CERN openlab Summer Student Programme",
                    ),
                ),
            ),
        ),
    ),
)
