package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_attachment_missing
import io.github.vietdsk6.porta.generated.resources.profile_close
import io.github.vietdsk6.porta.generated.resources.profile_current_pack
import io.github.vietdsk6.porta.generated.resources.profile_details
import io.github.vietdsk6.porta.generated.resources.profile_evidence_reference
import io.github.vietdsk6.porta.generated.resources.profile_growth_caveat
import io.github.vietdsk6.porta.generated.resources.profile_local_unverified
import io.github.vietdsk6.porta.generated.resources.profile_no_rule_uses
import io.github.vietdsk6.porta.generated.resources.profile_open_attachment
import io.github.vietdsk6.porta.generated.resources.profile_opportunity_references
import io.github.vietdsk6.porta.generated.resources.profile_remove_this_fact
import io.github.vietdsk6.porta.generated.resources.profile_rules_reference
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FactSection(
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
                    text = section.title.localizedProfileSection(),
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
internal fun FactRow(
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
                    text = fact.key.localizedLabel(),
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
                text = if (expanded) stringResource(Res.string.profile_close) else stringResource(Res.string.profile_details),
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
internal fun OpportunityCountBadge(count: Int) {
    Surface(
        color = AppTheme.colors.alternative,
        shape = CircleShape,
    ) {
        Text(
            text = pluralStringResource(Res.plurals.profile_opportunity_references, count, count),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = AppTheme.typography.bodyExtraSmall,
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun DependencyDetails(
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
                text = if (fact.dependentDoorTitles.isEmpty()) stringResource(Res.string.profile_current_pack) else stringResource(Res.string.profile_rules_reference),
                style = AppTheme.typography.bodyExtraSmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.7.sp,
            )
            if (fact.dependentDoorTitles.isEmpty()) {
                Text(
                    text = stringResource(Res.string.profile_no_rule_uses, fact.key.localizedLabel().lowercase()),
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
                text = "${fact.provenanceLabel.localizedProfileProvenance()} · ${fact.actionabilityLabel.localizedProfileActionability()}",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            fact.evidenceLabel?.let { evidence ->
                Text(
                    text = stringResource(Res.string.profile_evidence_reference, evidence),
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
                            text = stringResource(Res.string.profile_local_unverified),
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
                                Text(stringResource(Res.string.profile_open_attachment))
                            }
                        } else {
                            Text(
                                text = stringResource(Res.string.profile_attachment_missing),
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.text.secondary,
                            )
                        }
                    }
                }
            }
            if (fact.isGrowthEvidence) {
                Text(
                    text = stringResource(Res.string.profile_growth_caveat),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            TextButton(
                modifier = Modifier.testTag("student-profile-delete-${fact.key.name.lowercase()}"),
                onClick = onDelete,
            ) {
                Text(stringResource(Res.string.profile_remove_this_fact))
            }
        }
    }
}
