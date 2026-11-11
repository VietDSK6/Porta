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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import io.github.vietdsk6.porta.presentation.navigation.DesktopCommand
import io.github.vietdsk6.porta.presentation.navigation.LocalDesktopCommand

@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: StudentProfileViewModel,
    onEditProfile: () -> Unit,
    onOpenPackInspector: () -> Unit,
    onOpenDataPrivacy: () -> Unit,
    saveNotice: Boolean = false,
    onSaveNoticeDismissed: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val desktopRequest = LocalDesktopCommand.current
    LaunchedEffect(desktopRequest?.command) {
        if (desktopRequest?.command == DesktopCommand.ExportProfile) {
            viewModel.onUiEvent(StudentProfileUiEvent.ExportMetadata)
            desktopRequest.onHandled()
        }
    }

    StudentProfileScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onEditProfile = onEditProfile,
        onOpenPackInspector = onOpenPackInspector,
        onOpenDataPrivacy = onOpenDataPrivacy,
        saveNotice = saveNotice,
        onSaveNoticeDismissed = onSaveNoticeDismissed,
    )
}

@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
    onEditProfile: () -> Unit,
    onOpenPackInspector: () -> Unit,
    onOpenDataPrivacy: () -> Unit = {},
    saveNotice: Boolean = false,
    onSaveNoticeDismissed: () -> Unit = {},
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
                    onEditProfile = onEditProfile,
                    saveNotice = saveNotice,
                    onSaveNoticeDismissed = onSaveNoticeDismissed,
                )
            }

            if (!uiState.isLoading) {
                if (uiState.aiDraftSettingsAvailable) {
                    AiDraftSettingsCard(uiState = uiState, onUiEvent = onUiEvent)
                }
                DataPrivacyEntry(
                    isWorking = uiState.isWorking,
                    onClick = onOpenDataPrivacy,
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
                modifier = Modifier.fillMaxWidth().testTag("student-profile-explore-pack"),
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
    saveNotice: Boolean,
    onSaveNoticeDismissed: () -> Unit,
) {
    if (uiState.isDemoProfile) {
        DemoProfileNotice()
    }
    ProfileIdentity(uiState = uiState)

    if (saveNotice) {
        MessageNotice(
            message = "Profile saved on this device. Current opportunity checks will use these values.",
            onDismiss = onSaveNoticeDismissed,
        )
    }

    if (uiState.uncheckedFacts.isNotEmpty()) {
        UncheckedFactsNotice(
            facts = uiState.uncheckedFacts,
            onEditProfile = onEditProfile,
        )
    }

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
}

@Composable
private fun UncheckedFactsNotice(
    facts: List<UncheckedProfileFactUiState>,
    onEditProfile: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-unchecked"),
        color = AppTheme.colors.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.32f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (facts.size == 1) {
                    "A current rule also references ${facts.single().label.lowercase()}"
                } else {
                    "${facts.size} more details appear in current rules"
                },
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.SemiBold,
            )
            if (facts.size == 1) {
                val dependentCount = facts.single().dependentDoorCount
                Text(
                    text = "$dependentCount ${if (dependentCount == 1) "opportunity references it" else "opportunities reference it"}",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            facts.take(if (facts.size == 1) 0 else 3).forEach { fact ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = fact.label,
                        modifier = Modifier.weight(1f),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.primary,
                    )
                    Text(
                        text = when (fact.dependentDoorCount) {
                            1 -> "1 opportunity"
                            else -> "${fact.dependentDoorCount} opportunities"
                        },
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                }
            }
            if (facts.size > 3) {
                Text(
                    text = "+${facts.size - 3} more details in Edit profile",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Text(
                text = "A missing detail can leave a check unknown. It never means failed.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            TextButton(
                modifier = Modifier.testTag("student-profile-add-missing"),
                onClick = onEditProfile,
            ) {
                Text("Add profile facts")
            }
        }
    }
}

@Composable
private fun ProfileIdentity(uiState: StudentProfileUiState) {
    val facts = uiState.sections.flatMap(ProfileFactSectionUiState::facts)
    val fieldOfStudy = facts.valueOf(FactKey.FIELD_OF_STUDY)
    val studyLevel = facts.valueOf(FactKey.STUDY_LEVEL)?.sentenceCase()
    val nationality = facts.valueOf(FactKey.NATIONALITY)
    val identityLine = listOfNotNull(studyLevel, nationality).joinToString(" · ")

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
                        text = if (uiState.isDemoProfile) "FICTIONAL DEMO PROFILE" else "ACADEMIC PROFILE",
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

            ProfilePill("${uiState.factCount} ${if (uiState.factCount == 1) "detail" else "details"} provided")

            Text(
                text = when {
                    uiState.catalogOpportunityCount == 0 -> "No opportunities are in the current catalog."

                    uiState.dependentOpportunityCount == 0 -> {
                        "No current pack rules reference these details yet."
                    }

                    else -> {
                        "Rules in ${uiState.dependentOpportunityCount} of " +
                            "${uiState.catalogOpportunityCount} catalog opportunities reference your details."
                    }
                },
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )

            Text(
                text = "Saved on this device · references are not independently verified",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
private fun DemoProfileNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-demo-notice"),
        color = AppTheme.colors.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Ready-made demo profile",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "These fictional facts were saved locally for hands-on testing. You can edit them freely; clear the demo profile before entering personal data.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
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
                        onOpenEvidenceAttachment = {
                            onUiEvent(StudentProfileUiEvent.OpenEvidenceAttachment(fact.key))
                        },
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
    onOpenEvidenceAttachment: () -> Unit,
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

        if (fact.dependentDoorTitles.isNotEmpty()) {
            OpportunityCountBadge(fact.dependentDoorTitles.size)
        }

        if (expanded) {
            DependencyDetails(
                fact = fact,
                onDelete = onDelete,
                onOpenEvidenceAttachment = onOpenEvidenceAttachment,
            )
        }
    }
}

@Composable
private fun OpportunityCountBadge(count: Int) {
    Surface(
        color = AppTheme.colors.alternative,
        shape = CircleShape,
    ) {
        Text(
            text = when (count) {
                1 -> "1 opportunity references this"
                else -> "$count opportunities reference this"
            },
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = AppTheme.typography.bodyExtraSmall,
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DependencyDetails(
    fact: ProfileFactUiState,
    onDelete: () -> Unit,
    onOpenEvidenceAttachment: () -> Unit,
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
                text = if (fact.dependentDoorTitles.isEmpty()) "CURRENT PACK" else "RULES REFERENCE THIS IN",
                style = AppTheme.typography.bodyExtraSmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.7.sp,
            )
            if (fact.dependentDoorTitles.isEmpty()) {
                Text(
                    text = "No current pack rule references ${fact.label.lowercase()}. Other programmes may use it.",
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
            Text(
                text = "${fact.provenanceLabel} · ${fact.actionabilityLabel}",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            fact.evidenceLabel?.let { evidence ->
                Text(
                    text = "Evidence reference · $evidence",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            fact.evidenceAttachmentName?.let { attachmentName ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.surfaceContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.28f)),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = attachmentName,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.text.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        fact.evidenceAttachmentDetail?.let { detail ->
                            Text(
                                text = detail,
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.text.secondary,
                            )
                        }
                        Text(
                            text = "Stored locally · not independently verified",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.text.secondary,
                        )
                        if (fact.canOpenEvidenceAttachment) {
                            TextButton(
                                modifier = Modifier.testTag(
                                    "student-profile-open-evidence-${fact.key.name.lowercase()}",
                                ),
                                onClick = onOpenEvidenceAttachment,
                            ) {
                                Text("Open attachment")
                            }
                        } else {
                            Text(
                                text = "File not included in this device's imported profile copy.",
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.text.secondary,
                            )
                        }
                    }
                }
            }
            if (fact.isGrowthEvidence) {
                Text(
                    text = "Growth evidence does not automatically pass a published check.",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
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
private fun AiDraftSettingsCard(
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
) {
    var enteredKey by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-ai-drafting"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("AI drafting (optional)", style = AppTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Without a key, Opportunity Studio uses offline demo examples. AI only suggests rules; " +
                    "you approve each one, and Porta never uses AI to decide eligibility.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            Text(
                if (uiState.aiKeySaved) "A key is saved on this device." else "No key saved on this device.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            OutlinedTextField(
                value = enteredKey,
                onValueChange = { enteredKey = it },
                modifier = Modifier.fillMaxWidth().testTag("student-profile-ai-key-input"),
                label = { Text("Gemini API key") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !uiState.isWorking,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    modifier = Modifier.testTag("student-profile-ai-key-save"),
                    enabled = !uiState.isWorking && enteredKey.isNotBlank(),
                    onClick = {
                        val key = enteredKey
                        enteredKey = ""
                        onUiEvent(StudentProfileUiEvent.AiKeySaveRequested(key))
                    },
                ) {
                    Text("Save key")
                }
                if (uiState.aiKeySaved) {
                    TextButton(
                        modifier = Modifier.testTag("student-profile-ai-key-delete"),
                        enabled = !uiState.isWorking,
                        onClick = { onUiEvent(StudentProfileUiEvent.AiKeyDeleteRequested) },
                    ) {
                        Text("Delete key")
                    }
                }
            }
        }
    }
}

@Composable
private fun DataPrivacyEntry(
    isWorking: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student-profile-data-privacy")
            .clickable(enabled = !isWorking, role = Role.Button, onClick = onClick),
        color = AppTheme.colors.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Data & privacy",
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.text.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Inspect sources, export or remove local data",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Text(
                text = "Open",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

internal fun ProfileDataAction.confirmationTitle(): String = when (this) {
    ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> "Clear academic profile and plans?"
    ProfileDataAction.RESET_ALL_LOCAL_DATA -> "Reset Porta on this device?"
}

internal fun ProfileDataAction.confirmationBody(): String = when (this) {
    ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> {
        "This permanently removes profile facts, programme confirmations, and plans. " +
            "Saved doors, the opportunity catalog, and planning preferences stay on this device."
    }

    ProfileDataAction.RESET_ALL_LOCAL_DATA -> {
        "This permanently removes profile facts, programme confirmations, plans, saved doors, " +
            "imported opportunity catalogs, and preferences. Porta restores the bundled catalog; " +
            "onboarding may appear again next launch."
    }
}

internal fun ProfileDataAction.confirmationButton(): String = when (this) {
    ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> "Clear profile and plans"
    ProfileDataAction.RESET_ALL_LOCAL_DATA -> "Reset Porta"
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
            onOpenDataPrivacy = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun StudentProfileDemoScreenPreview() {
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

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun StudentProfileExpandedScreenPreview() {
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
private fun StudentProfileDataPrivacyEntryPreview() {
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

private val previewState = StudentProfileUiState(
    isLoading = false,
    hasRealProfile = true,
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
