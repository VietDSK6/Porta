package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityStudioDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioExpectedValueDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.StudioDeadlineKind
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.domain.model.StudioRootRuleKind

@Composable
fun OpportunityStudioScreen(
    viewModel: OpportunityStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OpportunityStudioScreen(
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun OpportunityStudioScreen(
    uiState: OpportunityStudioUiState,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.fillMaxSize().testTag("opportunity-studio-screen"),
        isScrollableContent = true,
        title = "Opportunity Studio",
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        when {
            !uiState.isSupported -> UnsupportedStudio()
            uiState.isLoading -> LoadingStudio()
            uiState.draft == null -> EmptyStudio(uiState.message)
            else -> StudioContent(uiState = uiState, onUiEvent = onUiEvent)
        }
    }
}

@Composable
private fun StudioContent(
    uiState: OpportunityStudioUiState,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    val draft = requireNotNull(uiState.draft)
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
        StudioIntro(uiState)
        AiDraftPanel(uiState = uiState, onUiEvent = onUiEvent)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val useTwoPane = opportunityStudioUsesTwoPane(maxWidth, LocalDensity.current.fontScale)
            if (useTwoPane) {
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-two-pane"),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
                    verticalAlignment = Alignment.Top,
                ) {
                    MetadataEditor(
                        draft = draft,
                        onUiEvent = onUiEvent,
                        modifier = Modifier.weight(0.9f),
                    )
                    RuleWorkspace(
                        uiState = uiState,
                        onUiEvent = onUiEvent,
                        modifier = Modifier.weight(1.1f),
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-stacked"),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
                ) {
                    MetadataEditor(draft = draft, onUiEvent = onUiEvent)
                    RuleWorkspace(uiState = uiState, onUiEvent = onUiEvent)
                }
            }
        }
    }
}

@Composable
private fun StudioIntro(uiState: OpportunityStudioUiState) {
    Surface(
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "SOURCE AUTHORING · SAFE DRAFT",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text("Add one source-linked opportunity", style = AppTheme.typography.h4)
            Text(
                "Studio clones ${uiState.basePackName} and appends this record. Export never changes the active catalog; " +
                    "Pack inspector still validates the diff and asks for approval.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            if (uiState.versionTransitionLabel.isNotBlank()) {
                Text(
                    uiState.versionTransitionLabel,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun MetadataEditor(
    draft: OpportunityStudioDraft,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    StudioPanel(modifier = modifier, title = "Opportunity metadata") {
        Text(
            "Use a stable ID and an official HTTPS source. Dates use ISO YYYY-MM-DD.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
        StudioTextField(
            value = draft.opportunityId,
            label = "Opportunity ID",
            tag = "opportunity-studio-id",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.OPPORTUNITY_ID, it) },
        )
        StudioTextField(
            value = draft.title,
            label = "Title",
            tag = "opportunity-studio-title",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.TITLE, it) },
        )
        StudioTextField(
            value = draft.provider,
            label = "Provider",
            tag = "opportunity-studio-provider",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.PROVIDER, it) },
        )
        EnumDropdown(
            label = "Category",
            value = draft.category,
            values = OpportunityCategory.entries,
            tag = "opportunity-studio-category",
            labelFor = { it.name.toDisplayLabel() },
            onSelect = { onUiEvent(OpportunityStudioUiEvent.CategoryChanged(it)) },
        )
        StudioTextField(
            value = draft.cycle,
            label = "Cycle",
            tag = "opportunity-studio-cycle",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.CYCLE, it) },
        )
        StudioTextField(
            value = draft.cycleStartOn,
            label = "Cycle start · optional",
            tag = "opportunity-studio-cycle-start",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.CYCLE_START_ON, it) },
        )
        StudioTextField(
            value = draft.sourceUrl,
            label = "Official source URL",
            tag = "opportunity-studio-source-url",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.SOURCE_URL, it) },
        )
        StudioTextField(
            value = draft.sourceSection,
            label = "Source section · optional",
            tag = "opportunity-studio-source-section",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.SOURCE_SECTION, it) },
        )
        StudioTextField(
            value = draft.lastCheckedOn,
            label = "Source checked on",
            tag = "opportunity-studio-checked-on",
            onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.LAST_CHECKED_ON, it) },
        )
        LabeledChoiceRow(label = "Deadline certainty") {
            StudioDeadlineKind.entries.forEach { kind ->
                FilterChip(
                    selected = draft.deadlineKind == kind,
                    onClick = { onUiEvent(OpportunityStudioUiEvent.DeadlineKindChanged(kind)) },
                    label = { Text(kind.name.toDisplayLabel()) },
                )
            }
        }
        if (draft.deadlineKind == StudioDeadlineKind.KNOWN) {
            StudioTextField(
                value = draft.deadlineDate,
                label = "Deadline",
                tag = "opportunity-studio-deadline",
                onValueChange = { onUiEvent.metadata(OpportunityStudioMetadataField.DEADLINE_DATE, it) },
            )
        }
    }
}

@Composable
private fun RuleWorkspace(
    uiState: OpportunityStudioUiState,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = requireNotNull(uiState.draft)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
        StudioPanel(title = "Eligibility rule builder") {
            Text(
                "This MVP builds one flat ALL/ANY group. Nested groups and milestone templates remain JSON-only.",
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            LabeledChoiceRow(label = "Root logic") {
                StudioRootRuleKind.entries.forEach { kind ->
                    FilterChip(
                        selected = draft.rootRuleKind == kind,
                        onClick = { onUiEvent(OpportunityStudioUiEvent.RootRuleKindChanged(kind)) },
                        label = { Text(kind.name) },
                        modifier = Modifier.testTag("opportunity-studio-root-${kind.name.lowercase()}"),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f).testTag("opportunity-studio-add-atomic"),
                    onClick = { onUiEvent(OpportunityStudioUiEvent.AddAtomicRule) },
                ) {
                    Text("Add atomic")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f).testTag("opportunity-studio-add-manual"),
                    onClick = { onUiEvent(OpportunityStudioUiEvent.AddManualRule) },
                ) {
                    Text("Add manual check")
                }
            }
            if (draft.rules.isEmpty()) {
                Text("Add at least one rule.", color = AppTheme.colors.status.warning)
            } else {
                draft.rules.forEachIndexed { index, rule ->
                    RuleSelector(
                        index = index,
                        rule = rule,
                        selected = uiState.selectedRuleIndex == index,
                        onSelect = { onUiEvent(OpportunityStudioUiEvent.RuleSelected(index)) },
                        onRemove = { onUiEvent(OpportunityStudioUiEvent.RuleRemoved(index)) },
                    )
                }
            }
            val selectedIndex = uiState.selectedRuleIndex
            val selectedRule = selectedIndex?.let(draft.rules::getOrNull)
            if (selectedIndex != null && selectedRule != null) {
                HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.35f))
                RuleEditor(
                    index = selectedIndex,
                    rule = selectedRule,
                    sourceNoteLocked = selectedRule.id in uiState.acceptedAiRuleIds,
                    onUiEvent = onUiEvent,
                )
            }
        }
        ValidationPanel(uiState = uiState, onUiEvent = onUiEvent)
    }
}

@Composable
private fun RuleSelector(
    index: Int,
    rule: OpportunityStudioRuleDraft,
    selected: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("opportunity-studio-rule-$index")
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .semantics { this.selected = selected },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) AppTheme.colors.alternative else AppTheme.colors.background,
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) AppTheme.colors.primary else AppTheme.colors.outline,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when (rule) {
                        is OpportunityStudioRuleDraft.Atomic -> "ATOMIC · ${rule.factKey.name.toDisplayLabel()}"
                        is OpportunityStudioRuleDraft.ManualCheck -> "MANUAL CHECK"
                    },
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    rule.id.ifBlank { "Rule ID required" },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onRemove) { Text("Remove") }
        }
    }
}

@Composable
private fun RuleEditor(
    index: Int,
    rule: OpportunityStudioRuleDraft,
    sourceNoteLocked: Boolean,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-rule-editor"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Edit selected rule", style = AppTheme.typography.h6)
        StudioTextField(
            value = rule.id,
            label = "Rule ID",
            tag = "opportunity-studio-rule-id",
            readOnly = sourceNoteLocked,
            onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.ID, it) },
        )
        if (sourceNoteLocked) {
            Text(
                "The accepted AI rule keeps its ID and grounded quote. Remove it and draft again to change either one.",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
        }
        when (rule) {
            is OpportunityStudioRuleDraft.Atomic -> AtomicRuleEditor(index, rule, sourceNoteLocked, onUiEvent)
            is OpportunityStudioRuleDraft.ManualCheck -> ManualRuleEditor(index, rule, sourceNoteLocked, onUiEvent)
        }
    }
}

@Composable
private fun AtomicRuleEditor(
    index: Int,
    rule: OpportunityStudioRuleDraft.Atomic,
    sourceNoteLocked: Boolean,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    EnumDropdown(
        label = "Profile fact",
        value = rule.factKey,
        values = FactKey.entries,
        tag = "opportunity-studio-fact-key",
        labelFor = { it.name.toDisplayLabel() },
        onSelect = { onUiEvent(OpportunityStudioUiEvent.AtomicFactKeyChanged(index, it)) },
    )
    EnumDropdown(
        label = "Operator",
        value = rule.operator,
        values = Operator.entries,
        tag = "opportunity-studio-operator",
        labelFor = { it.name.toDisplayLabel() },
        onSelect = { onUiEvent(OpportunityStudioUiEvent.AtomicOperatorChanged(index, it)) },
    )
    EnumDropdown(
        label = "Actionability",
        value = rule.actionability,
        values = Actionability.entries,
        tag = "opportunity-studio-actionability",
        labelFor = { it.name.toDisplayLabel() },
        onSelect = { onUiEvent(OpportunityStudioUiEvent.AtomicActionabilityChanged(index, it)) },
    )
    if (rule.operator != Operator.EXISTS) {
        val expected = rule.expected ?: OpportunityStudioExpectedValueDraft(StudioFactValueKind.TEXT)
        EnumDropdown(
            label = "Expected value type",
            value = expected.kind,
            values = StudioFactValueKind.entries,
            tag = "opportunity-studio-value-kind",
            labelFor = { it.name.toDisplayLabel() },
            onSelect = { onUiEvent(OpportunityStudioUiEvent.AtomicExpectedKindChanged(index, it)) },
        )
        StudioTextField(
            value = expected.value,
            label = expectedValueLabel(expected.kind),
            tag = "opportunity-studio-expected-value",
            supportingText = expectedValueHelp(expected.kind),
            onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.EXPECTED_VALUE, it) },
        )
        if (expected.kind == StudioFactValueKind.NUMBER || expected.kind == StudioFactValueKind.DURATION) {
            StudioTextField(
                value = expected.unit,
                label = if (expected.kind == StudioFactValueKind.DURATION) {
                    "Duration unit · HOURS/DAYS/MONTHS/YEARS"
                } else {
                    "Number unit or scale"
                },
                tag = "opportunity-studio-expected-unit",
                onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.EXPECTED_UNIT, it) },
            )
        }
    }
    StudioTextField(
        value = rule.explanation,
        label = "Plain-language explanation",
        tag = "opportunity-studio-rule-explanation",
        minLines = 2,
        onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.EXPLANATION, it) },
    )
    StudioTextField(
        value = rule.sourceNote,
        label = "Source note",
        tag = "opportunity-studio-rule-source-note",
        readOnly = sourceNoteLocked,
        minLines = 2,
        onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.SOURCE_NOTE, it) },
    )
}

@Composable
private fun ManualRuleEditor(
    index: Int,
    rule: OpportunityStudioRuleDraft.ManualCheck,
    sourceNoteLocked: Boolean,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    Text(
        "Manual checks always require human review and never auto-pass.",
        color = AppTheme.colors.text.secondary,
        style = AppTheme.typography.bodySmall,
    )
    StudioTextField(
        value = rule.explanation,
        label = "What must be reviewed?",
        tag = "opportunity-studio-rule-explanation",
        minLines = 2,
        onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.EXPLANATION, it) },
    )
    StudioTextField(
        value = rule.sourceNote,
        label = "Source note · optional",
        tag = "opportunity-studio-rule-source-note",
        readOnly = sourceNoteLocked,
        minLines = 2,
        onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.SOURCE_NOTE, it) },
    )
}

@Composable
private fun ValidationPanel(
    uiState: OpportunityStudioUiState,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    StudioPanel(title = "Validation and export") {
        if (uiState.diagnostics.isEmpty() && uiState.rawJson != null) {
            Text(
                "Ready to export · the full candidate passed Porta's pack parser and validator.",
                modifier = Modifier.testTag("opportunity-studio-valid"),
                color = AppTheme.colors.status.success,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Text(
                "${uiState.diagnostics.size} issue(s) must be resolved before export.",
                modifier = Modifier.testTag("opportunity-studio-invalid"),
                color = AppTheme.colors.status.warning,
                fontWeight = FontWeight.Bold,
            )
            uiState.diagnostics.take(MAX_VISIBLE_DIAGNOSTICS).forEach { diagnostic ->
                Surface(color = AppTheme.colors.background, shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                        Text(diagnostic.path, style = AppTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(diagnostic.message, style = AppTheme.typography.bodySmall)
                    }
                }
            }
            if (uiState.diagnostics.size > MAX_VISIBLE_DIAGNOSTICS) {
                Text(
                    "+${uiState.diagnostics.size - MAX_VISIBLE_DIAGNOSTICS} more issues",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
        }
        if (uiState.rawJson != null) {
            TextButton(
                modifier = Modifier.testTag("opportunity-studio-toggle-json"),
                onClick = { onUiEvent(OpportunityStudioUiEvent.ToggleJsonPreview) },
            ) {
                Text(if (uiState.isJsonPreviewVisible) "Hide JSON preview" else "Preview JSON")
            }
        }
        if (uiState.isJsonPreviewVisible && uiState.rawJson != null) {
            val preview = uiState.rawJson.take(JSON_PREVIEW_LIMIT)
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-json-preview"),
                color = AppTheme.colors.background,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = preview + if (preview.length < uiState.rawJson.length) "\n… preview truncated" else "",
                    modifier = Modifier.padding(12.dp),
                    style = AppTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
        AppButton(
            text = "Export JSON draft",
            modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-export"),
            enabled = uiState.canExport,
            isLoading = uiState.isWorking,
            onClick = { onUiEvent(OpportunityStudioUiEvent.ExportDraft) },
        )
        Text(
            "Export does not activate this pack. Return to Pack inspector, import the file, review the exact diff, then confirm.",
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
        uiState.message?.let { message ->
            Surface(color = AppTheme.colors.alternative, shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(message, modifier = Modifier.weight(1f), style = AppTheme.typography.bodySmall)
                    TextButton(onClick = { onUiEvent(OpportunityStudioUiEvent.MessageHandled) }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = AppTheme.typography.h5, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun StudioTextField(
    value: String,
    label: String,
    tag: String,
    onValueChange: (String) -> Unit,
    supportingText: String? = null,
    minLines: Int = 1,
    readOnly: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().testTag(tag),
        label = { Text(label) },
        supportingText = supportingText?.let { text -> ({ Text(text) }) },
        minLines = minLines,
        readOnly = readOnly,
        singleLine = minLines == 1,
    )
}

@Composable
private fun LabeledChoiceRow(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = AppTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun <T> EnumDropdown(
    label: String,
    value: T,
    values: List<T>,
    tag: String,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().testTag(tag),
            onClick = { expanded = true },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(label, style = AppTheme.typography.bodySmall, color = AppTheme.colors.text.secondary)
                Text(labelFor(value), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { option ->
                DropdownMenuItem(
                    text = { Text(labelFor(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun LoadingStudio() {
    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun UnsupportedStudio() {
    StudioPanel(title = "Authoring unavailable") {
        Text(
            "Opportunity Studio is unavailable on this device. You can still review opportunities and plan routes.",
            modifier = Modifier.testTag("opportunity-studio-unsupported"),
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
private fun EmptyStudio(message: String?) {
    StudioPanel(title = "Studio unavailable") {
        Text(message ?: "Restore or import an opportunity pack before opening Studio.")
    }
}

internal fun opportunityStudioUsesTwoPane(maxWidth: Dp, fontScale: Float): Boolean = maxWidth >= TWO_PANE_MIN_WIDTH && fontScale < LARGE_TEXT_SCALE

private fun ((OpportunityStudioUiEvent) -> Unit).metadata(
    field: OpportunityStudioMetadataField,
    value: String,
) = invoke(OpportunityStudioUiEvent.MetadataChanged(field, value))

private fun ((OpportunityStudioUiEvent) -> Unit).ruleText(
    ruleIndex: Int,
    field: OpportunityStudioRuleTextField,
    value: String,
) = invoke(OpportunityStudioUiEvent.RuleTextChanged(ruleIndex, field, value))

private fun String.toDisplayLabel(): String = lowercase()
    .replace('_', ' ')
    .replaceFirstChar(Char::uppercaseChar)

private fun expectedValueLabel(kind: StudioFactValueKind): String = when (kind) {
    StudioFactValueKind.BOOLEAN -> "Expected boolean · true/false"
    StudioFactValueKind.NUMBER -> "Expected number"
    StudioFactValueKind.ENUM -> "Expected enum value"
    StudioFactValueKind.ENUM_LIST -> "Expected values"
    StudioFactValueKind.TEXT -> "Expected text"
    StudioFactValueKind.DATE -> "Expected date · YYYY-MM-DD"
    StudioFactValueKind.DURATION -> "Expected duration amount"
}

private fun expectedValueHelp(kind: StudioFactValueKind): String? = when (kind) {
    StudioFactValueKind.ENUM_LIST -> "Separate values with commas or new lines."
    StudioFactValueKind.DATE -> "Use an unambiguous ISO date."
    StudioFactValueKind.DURATION -> "Enter a non-negative whole number."
    else -> null
}

@Preview(device = "spec:width=1280dp,height=1000dp,dpi=160")
@Composable
private fun OpportunityStudioDesktopPreview() {
    AppTheme {
        OpportunityStudioScreen(
            uiState = previewStudioState(),
            onUiEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(widthDp = 700, heightDp = 1000, fontScale = 2f)
@Composable
private fun OpportunityStudioLargeTextPreview() {
    AppTheme {
        OpportunityStudioScreen(
            uiState = previewStudioState(),
            onUiEvent = {},
            onNavigateBack = {},
        )
    }
}

private fun previewStudioState(): OpportunityStudioUiState = OpportunityStudioUiState(
    isLoading = false,
    basePackName = "Porta curated opportunities",
    basePackVersion = 3,
    nextPackVersion = 4,
    draft = OpportunityStudioDraft(
        opportunityId = "sample-scholarship-2027",
        title = "Sample source-linked scholarship",
        provider = "Example University",
        category = OpportunityCategory.SCHOLARSHIP,
        cycle = "2027",
        sourceUrl = "https://example.edu/scholarship",
        sourceSection = "Eligibility",
        lastCheckedOn = "2026-11-04",
        deadlineKind = StudioDeadlineKind.KNOWN,
        deadlineDate = "2027-03-01",
        rules = listOf(
            OpportunityStudioRuleDraft.Atomic(
                id = "ielts-minimum",
                factKey = FactKey.IELTS_SCORE,
                operator = Operator.GREATER_THAN_OR_EQUAL,
                expected = OpportunityStudioExpectedValueDraft(
                    kind = StudioFactValueKind.NUMBER,
                    value = "7.0",
                    unit = "band",
                ),
                explanation = "Applicants need an IELTS score of at least 7.0.",
                sourceNote = "Eligibility · English language requirement",
                actionability = Actionability.CONTROLLABLE,
            ),
            OpportunityStudioRuleDraft.ManualCheck(
                id = "academic-fit",
                explanation = "Review the published academic-fit guidance.",
            ),
        ),
    ),
    selectedRuleIndex = 0,
    diagnostics = listOf(
        OpportunityStudioDiagnosticUiState(
            path = "opportunities[15].eligibilityRule.children[1].sourceNote",
            message = "Add a source note before export.",
        ),
    ),
)

private const val MAX_VISIBLE_DIAGNOSTICS = 8
private const val JSON_PREVIEW_LIMIT = 6_000
private const val LARGE_TEXT_SCALE = 1.5f
private val TWO_PANE_MIN_WIDTH = 960.dp
