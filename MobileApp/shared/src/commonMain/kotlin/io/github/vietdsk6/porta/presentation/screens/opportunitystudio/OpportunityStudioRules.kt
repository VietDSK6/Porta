package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityStudioExpectedValueDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.domain.model.StudioRootRuleKind
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.studio_actionability
import io.github.vietdsk6.porta.generated.resources.studio_add_atomic
import io.github.vietdsk6.porta.generated.resources.studio_add_manual
import io.github.vietdsk6.porta.generated.resources.studio_add_rule_hint
import io.github.vietdsk6.porta.generated.resources.studio_ai_rule_locked
import io.github.vietdsk6.porta.generated.resources.studio_atomic_rule_label
import io.github.vietdsk6.porta.generated.resources.studio_duration_unit
import io.github.vietdsk6.porta.generated.resources.studio_edit_rule
import io.github.vietdsk6.porta.generated.resources.studio_expected_type
import io.github.vietdsk6.porta.generated.resources.studio_explanation
import io.github.vietdsk6.porta.generated.resources.studio_fact
import io.github.vietdsk6.porta.generated.resources.studio_manual_badge
import io.github.vietdsk6.porta.generated.resources.studio_manual_caveat
import io.github.vietdsk6.porta.generated.resources.studio_manual_question
import io.github.vietdsk6.porta.generated.resources.studio_number_unit
import io.github.vietdsk6.porta.generated.resources.studio_operator
import io.github.vietdsk6.porta.generated.resources.studio_remove
import io.github.vietdsk6.porta.generated.resources.studio_root_logic
import io.github.vietdsk6.porta.generated.resources.studio_rule_id
import io.github.vietdsk6.porta.generated.resources.studio_rule_id_required
import io.github.vietdsk6.porta.generated.resources.studio_rules_limit
import io.github.vietdsk6.porta.generated.resources.studio_rules_title
import io.github.vietdsk6.porta.generated.resources.studio_source_note
import io.github.vietdsk6.porta.generated.resources.studio_source_note_optional
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RuleWorkspace(
    uiState: OpportunityStudioUiState,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = requireNotNull(uiState.draft)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing)) {
        StudioPanel(title = stringResource(Res.string.studio_rules_title)) {
            Text(
                stringResource(Res.string.studio_rules_limit),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
            LabeledChoiceRow(label = stringResource(Res.string.studio_root_logic)) {
                StudioRootRuleKind.entries.forEach { kind ->
                    FilterChip(
                        selected = draft.rootRuleKind == kind,
                        onClick = { onUiEvent(OpportunityStudioUiEvent.RootRuleKindChanged(kind)) },
                        label = { Text(kind.localizedStudioLabel()) },
                        modifier = Modifier.testTag("opportunity-studio-root-${kind.name.lowercase()}"),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f).testTag("opportunity-studio-add-atomic"),
                    onClick = { onUiEvent(OpportunityStudioUiEvent.AddAtomicRule) },
                ) {
                    Text(stringResource(Res.string.studio_add_atomic))
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f).testTag("opportunity-studio-add-manual"),
                    onClick = { onUiEvent(OpportunityStudioUiEvent.AddManualRule) },
                ) {
                    Text(stringResource(Res.string.studio_add_manual))
                }
            }
            if (draft.rules.isEmpty()) {
                Text(stringResource(Res.string.studio_add_rule_hint), color = AppTheme.colors.status.warning)
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
internal fun RuleSelector(
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
                        is OpportunityStudioRuleDraft.Atomic -> stringResource(Res.string.studio_atomic_rule_label, rule.factKey.localizedStudioLabel())
                        is OpportunityStudioRuleDraft.ManualCheck -> stringResource(Res.string.studio_manual_badge)
                    },
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    rule.id.ifBlank { stringResource(Res.string.studio_rule_id_required) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onRemove) { Text(stringResource(Res.string.studio_remove)) }
        }
    }
}

@Composable
internal fun RuleEditor(
    index: Int,
    rule: OpportunityStudioRuleDraft,
    sourceNoteLocked: Boolean,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-rule-editor"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(stringResource(Res.string.studio_edit_rule), style = AppTheme.typography.h6)
        StudioTextField(
            value = rule.id,
            label = stringResource(Res.string.studio_rule_id),
            tag = "opportunity-studio-rule-id",
            readOnly = sourceNoteLocked,
            onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.ID, it) },
        )
        if (sourceNoteLocked) {
            Text(
                stringResource(Res.string.studio_ai_rule_locked),
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
internal fun AtomicRuleEditor(
    index: Int,
    rule: OpportunityStudioRuleDraft.Atomic,
    sourceNoteLocked: Boolean,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    EnumDropdown(
        label = stringResource(Res.string.studio_fact),
        value = rule.factKey,
        values = FactKey.entries,
        tag = "opportunity-studio-fact-key",
        labelFor = { it.localizedStudioLabel() },
        onSelect = { onUiEvent(OpportunityStudioUiEvent.AtomicFactKeyChanged(index, it)) },
    )
    EnumDropdown(
        label = stringResource(Res.string.studio_operator),
        value = rule.operator,
        values = Operator.entries,
        tag = "opportunity-studio-operator",
        labelFor = { it.localizedStudioLabel() },
        onSelect = { onUiEvent(OpportunityStudioUiEvent.AtomicOperatorChanged(index, it)) },
    )
    EnumDropdown(
        label = stringResource(Res.string.studio_actionability),
        value = rule.actionability,
        values = Actionability.entries,
        tag = "opportunity-studio-actionability",
        labelFor = { it.localizedStudioLabel() },
        onSelect = { onUiEvent(OpportunityStudioUiEvent.AtomicActionabilityChanged(index, it)) },
    )
    if (rule.operator != Operator.EXISTS) {
        val expected = rule.expected ?: OpportunityStudioExpectedValueDraft(StudioFactValueKind.TEXT)
        EnumDropdown(
            label = stringResource(Res.string.studio_expected_type),
            value = expected.kind,
            values = StudioFactValueKind.entries,
            tag = "opportunity-studio-value-kind",
            labelFor = { it.localizedStudioLabel() },
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
                    stringResource(Res.string.studio_duration_unit)
                } else {
                    stringResource(Res.string.studio_number_unit)
                },
                tag = "opportunity-studio-expected-unit",
                onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.EXPECTED_UNIT, it) },
            )
        }
    }
    StudioTextField(
        value = rule.explanation,
        label = stringResource(Res.string.studio_explanation),
        tag = "opportunity-studio-rule-explanation",
        minLines = 2,
        onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.EXPLANATION, it) },
    )
    StudioTextField(
        value = rule.sourceNote,
        label = stringResource(Res.string.studio_source_note),
        tag = "opportunity-studio-rule-source-note",
        readOnly = sourceNoteLocked,
        minLines = 2,
        onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.SOURCE_NOTE, it) },
    )
}

@Composable
internal fun ManualRuleEditor(
    index: Int,
    rule: OpportunityStudioRuleDraft.ManualCheck,
    sourceNoteLocked: Boolean,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    Text(
        stringResource(Res.string.studio_manual_caveat),
        color = AppTheme.colors.text.secondary,
        style = AppTheme.typography.bodySmall,
    )
    StudioTextField(
        value = rule.explanation,
        label = stringResource(Res.string.studio_manual_question),
        tag = "opportunity-studio-rule-explanation",
        minLines = 2,
        onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.EXPLANATION, it) },
    )
    StudioTextField(
        value = rule.sourceNote,
        label = stringResource(Res.string.studio_source_note_optional),
        tag = "opportunity-studio-rule-source-note",
        readOnly = sourceNoteLocked,
        minLines = 2,
        onValueChange = { onUiEvent.ruleText(index, OpportunityStudioRuleTextField.SOURCE_NOTE, it) },
    )
}
