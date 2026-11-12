package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityStudioDraft
import io.github.vietdsk6.porta.domain.model.StudioDeadlineKind
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.studio_category
import io.github.vietdsk6.porta.generated.resources.studio_checked_on
import io.github.vietdsk6.porta.generated.resources.studio_cycle
import io.github.vietdsk6.porta.generated.resources.studio_cycle_start
import io.github.vietdsk6.porta.generated.resources.studio_deadline
import io.github.vietdsk6.porta.generated.resources.studio_deadline_certainty
import io.github.vietdsk6.porta.generated.resources.studio_field_title
import io.github.vietdsk6.porta.generated.resources.studio_metadata_hint
import io.github.vietdsk6.porta.generated.resources.studio_metadata_title
import io.github.vietdsk6.porta.generated.resources.studio_official_url
import io.github.vietdsk6.porta.generated.resources.studio_opportunity_id
import io.github.vietdsk6.porta.generated.resources.studio_provider
import io.github.vietdsk6.porta.generated.resources.studio_source_section
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MetadataEditor(
    draft: OpportunityStudioDraft,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    StudioPanel(modifier = modifier, title = stringResource(Res.string.studio_metadata_title)) {
        Text(
            stringResource(Res.string.studio_metadata_hint),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
        StudioTextField(
            value = draft.opportunityId,
            label = stringResource(Res.string.studio_opportunity_id),
            tag = "opportunity-studio-id",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.OPPORTUNITY_ID, it) },
        )
        StudioTextField(
            value = draft.title,
            label = stringResource(Res.string.studio_field_title),
            tag = "opportunity-studio-title",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.TITLE, it) },
        )
        StudioTextField(
            value = draft.provider,
            label = stringResource(Res.string.studio_provider),
            tag = "opportunity-studio-provider",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.PROVIDER, it) },
        )
        EnumDropdown(
            label = stringResource(Res.string.studio_category),
            value = draft.category,
            values = OpportunityCategory.entries,
            tag = "opportunity-studio-category",
            labelFor = { it.localizedStudioLabel() },
            onSelect = { onUiEvent(OpportunityStudioUiEvent.CategoryChanged(it)) },
        )
        StudioTextField(
            value = draft.cycle,
            label = stringResource(Res.string.studio_cycle),
            tag = "opportunity-studio-cycle",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.CYCLE, it) },
        )
        StudioTextField(
            value = draft.cycleStartOn,
            label = stringResource(Res.string.studio_cycle_start),
            tag = "opportunity-studio-cycle-start",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.CYCLE_START_ON, it) },
        )
        StudioTextField(
            value = draft.sourceUrl,
            label = stringResource(Res.string.studio_official_url),
            tag = "opportunity-studio-source-url",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.SOURCE_URL, it) },
        )
        StudioTextField(
            value = draft.sourceSection,
            label = stringResource(Res.string.studio_source_section),
            tag = "opportunity-studio-source-section",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.SOURCE_SECTION, it) },
        )
        StudioTextField(
            value = draft.lastCheckedOn,
            label = stringResource(Res.string.studio_checked_on),
            tag = "opportunity-studio-checked-on",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.LAST_CHECKED_ON, it) },
        )
        LabeledChoiceRow(label = stringResource(Res.string.studio_deadline_certainty)) {
            StudioDeadlineKind.entries.forEach { kind ->
                FilterChip(
                    selected = draft.deadlineKind == kind,
                    onClick = { onUiEvent(OpportunityStudioUiEvent.DeadlineKindChanged(kind)) },
                    label = { Text(kind.localizedStudioLabel()) },
                )
            }
        }
        if (draft.deadlineKind == StudioDeadlineKind.KNOWN) {
            StudioTextField(
                value = draft.deadlineDate,
                label = stringResource(Res.string.studio_deadline),
                tag = "opportunity-studio-deadline",
                onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.DEADLINE_DATE, it) },
            )
        }
    }
}
