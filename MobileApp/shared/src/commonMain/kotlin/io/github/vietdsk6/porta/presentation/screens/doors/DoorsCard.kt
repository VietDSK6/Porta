package io.github.vietdsk6.porta.presentation.screens.doors

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.SourceFreshness
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.presentation.components.headlineText
import io.github.vietdsk6.porta.presentation.components.reasonText
import io.github.vietdsk6.porta.presentation.components.timingText
import io.github.vietdsk6.porta.presentation.locale.formatLocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DoorCard(
    door: DoorCardUiState,
    isHighlighted: Boolean,
    onOpen: () -> Unit,
    onSavedChanged: () -> Unit,
) {
    val stateColor = door.state.color()
    val savedDescription = if (door.isSaved) {
        stringResource(Res.string.doors_remove_saved_named, door.title)
    } else {
        stringResource(Res.string.doors_save_named, door.title)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("door-card-${door.id}")
            .clickable(role = Role.Button, onClick = onOpen),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) AppTheme.colors.alternative else AppTheme.colors.surfaceContainer,
        ),
        border = if (isHighlighted) BorderStroke(1.dp, AppTheme.colors.primary) else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (isHighlighted) {
                Text(
                    text = stringResource(Res.string.doors_updated_profile),
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        door.copyCode?.headlineText()?.uppercase() ?: door.stateLabel,
                        color = stateColor,
                        fontWeight = FontWeight.Bold,
                        style = AppTheme.typography.bodySmall,
                    )
                    Text(
                        door.title,
                        style = AppTheme.typography.h5,
                        color = AppTheme.colors.text.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = savedDescription
                        },
                    onClick = onSavedChanged,
                ) {
                    Text(
                        text = if (door.isSaved) "★" else "☆",
                        color = if (door.isSaved) AppTheme.colors.primary else AppTheme.colors.text.secondary,
                        style = AppTheme.typography.h5,
                    )
                }
            }
            Text(
                listOf(door.provider, door.category.displayLabel()).filter(String::isNotBlank).joinToString(" · "),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            Text(
                text = door.copyCode?.reasonText() ?: door.primaryReason,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                door.copyCode?.timingText() ?: door.deadlineLabel,
                color = AppTheme.colors.text.secondary,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodySmall,
            )
            if (door.sourceReviewNeedsAttention) {
                Text(
                    when (door.sourceFreshness) {
                        SourceFreshness.REVIEW_DUE -> stringResource(
                            Res.string.doors_source_review_due,
                            door.sourceCheckedOn?.let(::formatLocalDate).orEmpty(),
                        )

                        SourceFreshness.PREDATES_CYCLE -> stringResource(
                            Res.string.doors_source_review_predates,
                            door.sourceCheckedOn?.let(::formatLocalDate).orEmpty(),
                        )

                        SourceFreshness.FUTURE_DATED -> stringResource(
                            Res.string.doors_source_review_future,
                            door.sourceCheckedOn?.let(::formatLocalDate).orEmpty(),
                        )

                        else -> door.checkedLabel
                    },
                    color = AppTheme.colors.status.warning,
                    fontWeight = FontWeight.SemiBold,
                    style = AppTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun EligibilityState.color(): Color = when (this) {
    EligibilityState.OPEN -> AppTheme.colors.status.success

    EligibilityState.HAS_GAPS -> AppTheme.colors.status.warning

    EligibilityState.NEEDS_INFO -> AppTheme.colors.primary

    EligibilityState.NOT_ACTIONABLE,
    EligibilityState.CLOSED,
    -> AppTheme.colors.status.error
}

@Composable
internal fun OpportunityCategory.displayLabel(): String = when (this) {
    OpportunityCategory.SCHOLARSHIP -> stringResource(Res.string.doors_category_scholarship)
    OpportunityCategory.INTERNSHIP -> stringResource(Res.string.doors_category_internship)
    OpportunityCategory.ACADEMIC_PROGRAM -> stringResource(Res.string.doors_category_academic)
    OpportunityCategory.STUDENT_CONTEST -> stringResource(Res.string.doors_category_contest)
    OpportunityCategory.FELLOWSHIP -> stringResource(Res.string.doors_category_fellowship)
}
