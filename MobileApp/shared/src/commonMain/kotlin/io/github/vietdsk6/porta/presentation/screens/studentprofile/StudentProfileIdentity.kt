package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_add_facts
import io.github.vietdsk6.porta.generated.resources.profile_built_from
import io.github.vietdsk6.porta.generated.resources.profile_catalog_usage
import io.github.vietdsk6.porta.generated.resources.profile_demo_badge
import io.github.vietdsk6.porta.generated.resources.profile_demo_notice_body
import io.github.vietdsk6.porta.generated.resources.profile_demo_notice_title
import io.github.vietdsk6.porta.generated.resources.profile_demo_pack
import io.github.vietdsk6.porta.generated.resources.profile_demo_pack_notice
import io.github.vietdsk6.porta.generated.resources.profile_demo_remove
import io.github.vietdsk6.porta.generated.resources.profile_detail_provided
import io.github.vietdsk6.porta.generated.resources.profile_field_student
import io.github.vietdsk6.porta.generated.resources.profile_local_references
import io.github.vietdsk6.porta.generated.resources.profile_missing_caveat
import io.github.vietdsk6.porta.generated.resources.profile_missing_multiple
import io.github.vietdsk6.porta.generated.resources.profile_missing_single
import io.github.vietdsk6.porta.generated.resources.profile_more_details
import io.github.vietdsk6.porta.generated.resources.profile_no_opportunities
import io.github.vietdsk6.porta.generated.resources.profile_no_references
import io.github.vietdsk6.porta.generated.resources.profile_opportunity_count
import io.github.vietdsk6.porta.generated.resources.profile_real_badge
import io.github.vietdsk6.porta.generated.resources.profile_references_count
import io.github.vietdsk6.porta.generated.resources.profile_student_title
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UncheckedFactsNotice(
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
                    stringResource(Res.string.profile_missing_single, facts.single().key.localizedLabel().lowercase())
                } else {
                    pluralStringResource(Res.plurals.profile_missing_multiple, facts.size, facts.size)
                },
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.primary,
                fontWeight = FontWeight.SemiBold,
            )
            if (facts.size == 1) {
                val dependentCount = facts.single().dependentDoorCount
                Text(
                    text = pluralStringResource(Res.plurals.profile_references_count, dependentCount, dependentCount),
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
                        text = fact.key.localizedLabel(),
                        modifier = Modifier.weight(1f),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.primary,
                    )
                    Text(
                        text = pluralStringResource(
                            Res.plurals.profile_opportunity_count,
                            fact.dependentDoorCount,
                            fact.dependentDoorCount,
                        ),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                }
            }
            if (facts.size > 3) {
                Text(
                    text = stringResource(Res.string.profile_more_details, facts.size - 3),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Text(
                text = stringResource(Res.string.profile_missing_caveat),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            TextButton(
                modifier = Modifier.testTag("student-profile-add-missing"),
                onClick = onEditProfile,
            ) {
                Text(stringResource(Res.string.profile_add_facts))
            }
        }
    }
}

@Composable
internal fun ProfileIdentity(uiState: StudentProfileUiState) {
    val facts = uiState.sections.flatMap(ProfileFactSectionUiState::facts)
    val fieldOfStudy = facts.firstOrNull { it.key == FactKey.FIELD_OF_STUDY }?.localizedValue(uiState.isDemoProfile)
    val studyLevel = facts.firstOrNull { it.key == FactKey.STUDY_LEVEL }?.localizedValue(uiState.isDemoProfile)
    val nationality = facts.firstOrNull { it.key == FactKey.NATIONALITY }?.localizedValue(uiState.isDemoProfile)
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
                        text = if (uiState.isDemoProfile) stringResource(Res.string.profile_demo_badge) else stringResource(Res.string.profile_real_badge),
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = fieldOfStudy?.let { stringResource(Res.string.profile_field_student, it) }
                            ?: stringResource(Res.string.profile_student_title),
                        style = AppTheme.typography.h5,
                        color = AppTheme.colors.text.primary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = identityLine.ifEmpty { stringResource(Res.string.profile_built_from) },
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.text.secondary,
                    )
                }
            }

            ProfilePill(pluralStringResource(Res.plurals.profile_detail_provided, uiState.factCount, uiState.factCount))

            Text(
                text = when {
                    uiState.catalogOpportunityCount == 0 -> stringResource(Res.string.profile_no_opportunities)

                    uiState.dependentOpportunityCount == 0 -> {
                        stringResource(Res.string.profile_no_references)
                    }

                    else -> {
                        stringResource(
                            Res.string.profile_catalog_usage,
                            uiState.dependentOpportunityCount,
                            uiState.catalogOpportunityCount,
                        )
                    }
                },
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )

            Text(
                text = stringResource(Res.string.profile_local_references),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}

@Composable
internal fun DemoProfileNotice(
    isWorking: Boolean,
    onRemove: () -> Unit,
) {
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
                text = stringResource(Res.string.profile_demo_notice_title),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.profile_demo_notice_body),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            TextButton(
                modifier = Modifier.testTag("student-profile-remove-demo"),
                enabled = !isWorking,
                onClick = onRemove,
            ) {
                Text(stringResource(Res.string.profile_demo_remove))
            }
        }
    }
}

@Composable
internal fun ProfileAvatar(monogram: String) {
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
internal fun ProfilePill(label: String) {
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
internal fun DemoPackNotice() {
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
                    text = stringResource(Res.string.profile_demo_pack),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = AppTheme.typography.bodyExtraSmall,
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                )
            }
            Text(
                text = stringResource(Res.string.profile_demo_pack_notice),
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }
    }
}
