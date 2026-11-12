package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_setup_add
import io.github.vietdsk6.porta.generated.resources.profile_setup_collapsed
import io.github.vietdsk6.porta.generated.resources.profile_setup_data_privacy
import io.github.vietdsk6.porta.generated.resources.profile_setup_expanded
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_age
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_availability
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_birth
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_degree
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_enrolled
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_experience
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_gpa
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_ielts
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_leadership
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_nationality
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_project
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_residence
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_study
import io.github.vietdsk6.porta.generated.resources.profile_setup_field_study_level
import io.github.vietdsk6.porta.generated.resources.profile_setup_fix
import io.github.vietdsk6.porta.generated.resources.profile_setup_hide
import io.github.vietdsk6.porta.generated.resources.profile_setup_local_saved
import io.github.vietdsk6.porta.generated.resources.profile_setup_no
import io.github.vietdsk6.porta.generated.resources.profile_setup_provided_optional
import io.github.vietdsk6.porta.generated.resources.profile_setup_provided_review
import io.github.vietdsk6.porta.generated.resources.profile_setup_review
import io.github.vietdsk6.porta.generated.resources.profile_setup_unsure
import io.github.vietdsk6.porta.generated.resources.profile_setup_yes
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BooleanFactField(
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
                label = { Text(stringResource(Res.string.profile_setup_yes)) },
            )
            FilterChip(
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                selected = value == false,
                onClick = { onChanged(false) },
                label = { Text(stringResource(Res.string.profile_setup_no)) },
            )
            if (includeUnknown) {
                FilterChip(
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    selected = value == null,
                    onClick = { onChanged(null) },
                    label = { Text(stringResource(Res.string.profile_setup_unsure)) },
                )
            }
        }
    }
}

@Composable
internal fun FormSection(
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
internal fun ProgressiveFormSection(
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
    val expandedDescription = stringResource(Res.string.profile_setup_expanded)
    val collapsedDescription = stringResource(Res.string.profile_setup_collapsed, status)
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
                        stateDescription = if (isExpanded) expandedDescription else collapsedDescription
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
                        isExpanded -> stringResource(Res.string.profile_setup_hide)
                        needsReview -> stringResource(Res.string.profile_setup_fix)
                        providedCount == 0 -> stringResource(Res.string.profile_setup_add)
                        else -> stringResource(Res.string.profile_setup_review)
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
internal fun LocalDataNotice(onOpenDataPrivacy: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(Res.string.profile_setup_local_saved),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
        TextButton(
            onClick = onOpenDataPrivacy,
            modifier = Modifier.testTag("profile-setup-data-privacy"),
        ) {
            Text(stringResource(Res.string.profile_setup_data_privacy))
        }
    }
}

internal enum class ProfileSetupSection {
    ACADEMIC,
    EXPERIENCE,
    CONTEXT,
}

internal fun FactKey.setupSection(): ProfileSetupSection? = when (this) {
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

@Composable
internal fun FactKey.profileFieldLabel(): String = stringResource(
    when (this) {
        FactKey.GPA -> Res.string.profile_setup_field_gpa
        FactKey.STUDY_LEVEL -> Res.string.profile_setup_field_study_level
        FactKey.FIELD_OF_STUDY -> Res.string.profile_setup_field_study
        FactKey.NATIONALITY -> Res.string.profile_setup_field_nationality
        FactKey.RESIDENCY -> Res.string.profile_setup_field_residence
        FactKey.IELTS_SCORE -> Res.string.profile_setup_field_ielts
        FactKey.EXPERIENCE_MONTHS -> Res.string.profile_setup_field_experience
        FactKey.COMMUNITY_PROJECT -> Res.string.profile_setup_field_project
        FactKey.LEADERSHIP -> Res.string.profile_setup_field_leadership
        FactKey.ENROLLED -> Res.string.profile_setup_field_enrolled
        FactKey.AGE -> Res.string.profile_setup_field_age
        FactKey.DATE_OF_BIRTH -> Res.string.profile_setup_field_birth
        FactKey.DEGREE_COMPLETED -> Res.string.profile_setup_field_degree
        FactKey.AVAILABLE_FULL_PERIOD -> Res.string.profile_setup_field_availability
    },
)

@Composable
private fun Int.providedStatus(total: Int, needsReview: Boolean = false): String = stringResource(
    if (needsReview) Res.string.profile_setup_provided_review else Res.string.profile_setup_provided_optional,
    this,
    total,
)
