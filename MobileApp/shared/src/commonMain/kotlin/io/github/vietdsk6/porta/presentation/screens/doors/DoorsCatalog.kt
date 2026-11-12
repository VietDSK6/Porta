package io.github.vietdsk6.porta.presentation.screens.doors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.components.porta.PortaArch
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.generated.resources.*
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileAndPackContext(
    isSampleProfile: Boolean,
    isDemoProfile: Boolean,
    isSampleCatalog: Boolean,
    doorCount: Int,
    gapCount: Int,
    sourceReviewDueCount: Int,
) {
    val summary = pluralStringResource(Res.plurals.doors_summary, doorCount, doorCount, gapCount)
    if (!isSampleProfile && !isDemoProfile && !isSampleCatalog) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = summary,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall,
            )
            if (sourceReviewDueCount > 0) {
                Text(
                    text = pluralStringResource(
                        Res.plurals.doors_source_review_count,
                        sourceReviewDueCount,
                        sourceReviewDueCount,
                    ),
                    color = AppTheme.colors.status.warning,
                    style = AppTheme.typography.bodySmall,
                )
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            when {
                isDemoProfile && isSampleCatalog -> stringResource(Res.string.doors_demo_synthetic)
                isDemoProfile -> stringResource(Res.string.doors_demo_sourced)
                isSampleProfile && isSampleCatalog -> stringResource(Res.string.doors_sample_synthetic)
                isSampleProfile -> stringResource(Res.string.doors_sample_profile)
                isSampleCatalog -> stringResource(Res.string.doors_own_synthetic)
                else -> stringResource(Res.string.doors_own_profile)
            },
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodySmall,
        )
        Text(
            summary,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
        )
        if (isSampleCatalog) {
            Text(stringResource(Res.string.doors_fictional_opportunities), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
        if (isDemoProfile) {
            Text(stringResource(Res.string.doors_fictional_profile), color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun DoorsEmptyState(
    selectedFilter: DoorFilter,
    hasCatalogCriteria: Boolean,
    onShowAll: () -> Unit,
) {
    val title = when {
        hasCatalogCriteria -> stringResource(Res.string.doors_empty_search)
        selectedFilter == DoorFilter.SAVED -> stringResource(Res.string.doors_empty_saved)
        selectedFilter == DoorFilter.ALL -> stringResource(Res.string.doors_empty_pack)
        else -> stringResource(Res.string.doors_empty_filter)
    }
    val body = when {
        hasCatalogCriteria -> stringResource(Res.string.doors_empty_search_help)
        selectedFilter == DoorFilter.SAVED -> stringResource(Res.string.doors_empty_saved_help)
        selectedFilter == DoorFilter.ALL -> stringResource(Res.string.doors_empty_pack_help)
        else -> stringResource(Res.string.doors_empty_filter_help)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doors-empty-${selectedFilter.name.lowercase()}")
            .padding(horizontal = 12.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PortaArch(
            modifier = Modifier.size(width = 64.dp, height = 76.dp),
            containerColor = AppTheme.colors.alternative,
            outlineColor = AppTheme.colors.outline,
        ) {
            Text(
                text = if (selectedFilter == DoorFilter.SAVED) "☆" else "—",
                color = AppTheme.colors.primary,
                style = AppTheme.typography.h5,
            )
        }
        Text(title, color = AppTheme.colors.text.primary, style = AppTheme.typography.h5)
        Text(
            text = body,
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (selectedFilter != DoorFilter.ALL || hasCatalogCriteria) {
            TextButton(
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("doors-empty-show-all"),
                onClick = onShowAll,
            ) {
                Text(stringResource(Res.string.doors_show_all))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DoorCatalogControls(
    searchQuery: String,
    selectedCategory: OpportunityCategory?,
    selectedDeadlineFilter: DoorDeadlineFilter,
    onSearchChanged: (String) -> Unit,
    onCategoryChanged: (OpportunityCategory?) -> Unit,
    onDeadlineChanged: (DoorDeadlineFilter) -> Unit,
    searchFocusRequester: FocusRequester,
) {
    var showMore by rememberSaveable {
        mutableStateOf(selectedCategory != null || selectedDeadlineFilter != DoorDeadlineFilter.ALL)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(searchFocusRequester)
                .testTag("doors-search"),
            value = searchQuery,
            onValueChange = onSearchChanged,
            singleLine = true,
            label = { Text(stringResource(Res.string.doors_search)) },
            supportingText = { Text(stringResource(Res.string.doors_search_help)) },
        )
        TextButton(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .testTag("doors-catalog-filters-toggle"),
            onClick = { showMore = !showMore },
        ) {
            Text(if (showMore) stringResource(Res.string.doors_hide_filters) else stringResource(Res.string.doors_show_filters))
        }
        if (showMore) {
            Text(
                text = stringResource(Res.string.doors_category),
                color = AppTheme.colors.text.secondary,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodySmall,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CategoryFilterChip(
                    category = null,
                    selectedCategory = selectedCategory,
                    onSelected = onCategoryChanged,
                )
                OpportunityCategory.entries.forEach { category ->
                    CategoryFilterChip(category, selectedCategory, onCategoryChanged)
                }
            }
            Text(
                text = stringResource(Res.string.doors_deadline),
                color = AppTheme.colors.text.secondary,
                fontWeight = FontWeight.SemiBold,
                style = AppTheme.typography.bodySmall,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                DoorDeadlineFilter.entries.forEach { filter ->
                    FilterChip(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("door-deadline-filter-${filter.name}"),
                        selected = filter == selectedDeadlineFilter,
                        onClick = { onDeadlineChanged(filter) },
                        label = { Text(filter.displayLabel(), style = AppTheme.typography.bodySmall) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterChip(
    category: OpportunityCategory?,
    selectedCategory: OpportunityCategory?,
    onSelected: (OpportunityCategory?) -> Unit,
) {
    val label = category?.displayLabel() ?: stringResource(Res.string.doors_all_categories)
    FilterChip(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("door-category-filter-${category?.name ?: "ALL"}"),
        selected = category == selectedCategory,
        onClick = { onSelected(category) },
        label = { Text(label, style = AppTheme.typography.bodySmall) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DoorFilterRow(selected: DoorFilter, onSelected: (DoorFilter) -> Unit) {
    val primaryFilters = listOf(DoorFilter.ALL, DoorFilter.GAPS, DoorFilter.VERIFY, DoorFilter.SAVED)
    val secondaryFilters = DoorFilter.entries.filterNot(primaryFilters::contains)
    var showMore by rememberSaveable { mutableStateOf(selected in secondaryFilters) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            primaryFilters.forEach { filter -> DoorFilterChip(filter, selected, onSelected) }
        }
        TextButton(
            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            onClick = { showMore = !showMore },
        ) {
            Text(if (showMore) stringResource(Res.string.doors_fewer_filters) else stringResource(Res.string.doors_more_filters))
        }
        if (showMore) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                secondaryFilters.forEach { filter -> DoorFilterChip(filter, selected, onSelected) }
            }
        }
    }
}

@Composable
private fun DoorFilterChip(
    filter: DoorFilter,
    selected: DoorFilter,
    onSelected: (DoorFilter) -> Unit,
) {
    FilterChip(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("door-filter-${filter.name}"),
        selected = filter == selected,
        onClick = { onSelected(filter) },
        label = {
            Text(
                when (filter) {
                    DoorFilter.ALL -> stringResource(Res.string.doors_filter_all)
                    DoorFilter.GAPS -> stringResource(Res.string.doors_filter_action)
                    DoorFilter.VERIFY -> stringResource(Res.string.doors_filter_verify)
                    DoorFilter.SAVED -> stringResource(Res.string.doors_filter_saved)
                    DoorFilter.OPEN -> stringResource(Res.string.doors_filter_open)
                    DoorFilter.NOT_AVAILABLE -> stringResource(Res.string.doors_filter_unavailable)
                    DoorFilter.CLOSED -> stringResource(Res.string.doors_filter_closed)
                    DoorFilter.ARCHIVED -> stringResource(Res.string.doors_filter_archived)
                },
                style = AppTheme.typography.bodySmall,
            )
        },
    )
}

@Composable
private fun DoorDeadlineFilter.displayLabel(): String = when (this) {
    DoorDeadlineFilter.ALL -> stringResource(Res.string.doors_deadline_any)
    DoorDeadlineFilter.UPCOMING -> stringResource(Res.string.doors_deadline_upcoming)
    DoorDeadlineFilter.TBA_OR_MISSING -> stringResource(Res.string.doors_deadline_tba)
    DoorDeadlineFilter.CLOSED -> stringResource(Res.string.doors_deadline_closed)
}
