package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ConfirmationChecklist(uiState: ActualProfileUpdateUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("actual-result-checklist"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(Res.string.actual_checklist_title), style = AppTheme.typography.h6)
            ChecklistStep(
                number = 1,
                title = if (uiState.inputKind == ActualValueInputKind.BOOLEAN) {
                    stringResource(Res.string.actual_select_result)
                } else {
                    stringResource(Res.string.actual_enter_result)
                },
                status = if (uiState.hasActualResult) stringResource(Res.string.actual_done) else stringResource(Res.string.actual_next),
                isDone = uiState.hasActualResult,
            )
            val evidenceStatus = when {
                uiState.hasSupportingEvidence -> stringResource(Res.string.actual_added)

                uiState.inputKind == ActualValueInputKind.BOOLEAN && uiState.achievedBoolean == false ->
                    stringResource(Res.string.actual_not_needed_no)

                uiState.isEvidenceRequired &&
                    uiState.inputKind == ActualValueInputKind.BOOLEAN &&
                    uiState.achievedBoolean == null -> stringResource(Res.string.actual_required_if_yes)

                uiState.requiresSupportingEvidence -> stringResource(Res.string.actual_required)

                else -> stringResource(Res.string.actual_optional)
            }
            ChecklistStep(
                number = 2,
                title = stringResource(Res.string.actual_add_evidence),
                status = evidenceStatus,
                isDone = uiState.hasSupportingEvidence ||
                    (uiState.inputKind == ActualValueInputKind.BOOLEAN && uiState.achievedBoolean == false),
            )
        }
    }
}

@Composable
private fun ChecklistStep(
    number: Int,
    title: String,
    status: String,
    isDone: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isDone) "✓" else number.toString(),
            color = AppTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.h6,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = AppTheme.typography.bodyLarge)
            Text(status, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun BoundaryBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                stringResource(Res.string.actual_boundary_label),
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall,
            )
            Text(
                stringResource(Res.string.actual_boundary_body),
                color = AppTheme.colors.onAlternative,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
internal fun ValueComparisonCard(uiState: ActualProfileUpdateUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(uiState.localizedFactLabel(), style = AppTheme.typography.h5)
            Text(uiState.planTitle, color = AppTheme.colors.text.secondary, style = AppTheme.typography.bodyMedium)
            val scope = uiState.localizedScope()
            scope?.let {
                Text(
                    text = scope,
                    color = AppTheme.colors.primary,
                    style = AppTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ValueTile(
                    eyebrow = stringResource(Res.string.actual_current_actual),
                    value = uiState.localizedCurrentValue(),
                    modifier = Modifier.weight(1f),
                )
                ValueTile(
                    eyebrow = stringResource(Res.string.actual_planned_target),
                    value = uiState.plannedTargetLabel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ValueTile(
    eyebrow: String,
    value: String,
    modifier: Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                eyebrow,
                color = AppTheme.colors.text.secondary,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodySmall,
            )
            Text(value, color = AppTheme.colors.text.primary, style = AppTheme.typography.h6)
        }
    }
}

@Composable
internal fun AchievedValueCard(
    uiState: ActualProfileUpdateUiState,
    onUiEvent: (ActualProfileUpdateUiEvent) -> Unit,
) {
    val localizedUnit = uiState.localizedUnit()
    val inputStateDescription = uiState.inputKind.inputStateDescription(localizedUnit)
    val validationError = uiState.localizedValidationError()
    FormCard(
        title = stringResource(Res.string.actual_achieved_question),
        description = stringResource(Res.string.actual_achieved_help),
    ) {
        when (uiState.inputKind) {
            ActualValueInputKind.BOOLEAN -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    modifier = Modifier.testTag("achieved-value-yes"),
                    selected = uiState.achievedBoolean == true,
                    onClick = { onUiEvent(ActualProfileUpdateUiEvent.AchievedBooleanChanged(true)) },
                    label = { Text(stringResource(Res.string.actual_yes)) },
                )
                FilterChip(
                    modifier = Modifier.testTag("achieved-value-no"),
                    selected = uiState.achievedBoolean == false,
                    onClick = { onUiEvent(ActualProfileUpdateUiEvent.AchievedBooleanChanged(false)) },
                    label = { Text(stringResource(Res.string.actual_no)) },
                )
            }

            ActualValueInputKind.NUMBER,
            ActualValueInputKind.ENUM,
            ActualValueInputKind.ENUM_LIST,
            ActualValueInputKind.TEXT,
            ActualValueInputKind.DATE,
            ActualValueInputKind.DURATION,
            -> OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("achieved-value-input")
                    .semantics {
                        inputStateDescription?.let {
                            stateDescription = it
                        }
                    },
                value = uiState.achievedValue,
                onValueChange = { onUiEvent(ActualProfileUpdateUiEvent.AchievedValueChanged(it)) },
                label = { Text(uiState.inputKind.inputLabel()) },
                placeholder = uiState.inputKind.inputPlaceholder()?.let { placeholder ->
                    { Text(placeholder) }
                },
                suffix = localizedUnit.takeIf(String::isNotBlank)?.let { unit ->
                    { Text(unit) }
                },
                supportingText = (validationError ?: uiState.inputKind.inputGuidance())?.let { message ->
                    { Text(message) }
                },
                isError = uiState.achievedValueError != null,
                keyboardOptions = KeyboardOptions(keyboardType = uiState.inputKind.keyboardType()),
                singleLine = uiState.inputKind != ActualValueInputKind.ENUM_LIST,
                minLines = if (uiState.inputKind == ActualValueInputKind.ENUM_LIST) 2 else 1,
            )
        }
    }
}

@Composable
private fun ActualValueInputKind.inputLabel(): String = when (this) {
    ActualValueInputKind.NUMBER -> stringResource(Res.string.actual_number)
    ActualValueInputKind.BOOLEAN -> stringResource(Res.string.actual_value)
    ActualValueInputKind.ENUM -> stringResource(Res.string.actual_option)
    ActualValueInputKind.ENUM_LIST -> stringResource(Res.string.actual_values)
    ActualValueInputKind.TEXT -> stringResource(Res.string.actual_value)
    ActualValueInputKind.DATE -> stringResource(Res.string.actual_date)
    ActualValueInputKind.DURATION -> stringResource(Res.string.actual_duration)
}

@Composable
private fun ActualValueInputKind.inputPlaceholder(): String? = when (this) {
    ActualValueInputKind.DATE -> stringResource(Res.string.actual_date_placeholder)
    ActualValueInputKind.ENUM_LIST -> stringResource(Res.string.actual_values_placeholder)
    else -> null
}

@Composable
private fun ActualValueInputKind.inputGuidance(): String? = when (this) {
    ActualValueInputKind.DATE -> stringResource(Res.string.actual_date_help)
    ActualValueInputKind.DURATION -> stringResource(Res.string.actual_duration_help)
    ActualValueInputKind.ENUM_LIST -> stringResource(Res.string.actual_values_help)
    else -> null
}

@Composable
private fun ActualValueInputKind.inputStateDescription(unitLabel: String): String? = when (this) {
    ActualValueInputKind.DATE -> stringResource(Res.string.actual_date_state)
    ActualValueInputKind.DURATION -> stringResource(Res.string.actual_duration_state, unitLabel)
    ActualValueInputKind.ENUM_LIST -> stringResource(Res.string.actual_values_state)
    else -> null
}

private fun ActualValueInputKind.keyboardType(): KeyboardType = when (this) {
    ActualValueInputKind.NUMBER -> KeyboardType.Decimal
    ActualValueInputKind.DURATION -> KeyboardType.Number
    else -> KeyboardType.Text
}
