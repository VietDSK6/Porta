package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_dismiss
import io.github.vietdsk6.porta.generated.resources.studio_authoring_unavailable
import io.github.vietdsk6.porta.generated.resources.studio_device_unavailable
import io.github.vietdsk6.porta.generated.resources.studio_expected_boolean
import io.github.vietdsk6.porta.generated.resources.studio_expected_date
import io.github.vietdsk6.porta.generated.resources.studio_expected_duration
import io.github.vietdsk6.porta.generated.resources.studio_expected_enum
import io.github.vietdsk6.porta.generated.resources.studio_expected_help_date
import io.github.vietdsk6.porta.generated.resources.studio_expected_help_duration
import io.github.vietdsk6.porta.generated.resources.studio_expected_help_values
import io.github.vietdsk6.porta.generated.resources.studio_expected_number
import io.github.vietdsk6.porta.generated.resources.studio_expected_text
import io.github.vietdsk6.porta.generated.resources.studio_expected_values
import io.github.vietdsk6.porta.generated.resources.studio_export_caveat
import io.github.vietdsk6.porta.generated.resources.studio_export_json
import io.github.vietdsk6.porta.generated.resources.studio_issue_count
import io.github.vietdsk6.porta.generated.resources.studio_json_hide
import io.github.vietdsk6.porta.generated.resources.studio_json_preview
import io.github.vietdsk6.porta.generated.resources.studio_more_issues
import io.github.vietdsk6.porta.generated.resources.studio_pack_missing
import io.github.vietdsk6.porta.generated.resources.studio_preview_truncated
import io.github.vietdsk6.porta.generated.resources.studio_unavailable
import io.github.vietdsk6.porta.generated.resources.studio_valid_notice
import io.github.vietdsk6.porta.generated.resources.studio_validation_title
import io.github.vietdsk6.porta.presentation.theme.PortaBreakpoints
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ValidationPanel(
    uiState: OpportunityStudioUiState,
    onUiEvent: (OpportunityStudioUiEvent) -> Unit,
) {
    StudioPanel(title = stringResource(Res.string.studio_validation_title)) {
        if (uiState.diagnostics.isEmpty() && uiState.rawJson != null) {
            Text(
                stringResource(Res.string.studio_valid_notice),
                modifier = Modifier.testTag("opportunity-studio-valid"),
                color = AppTheme.colors.status.success,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Text(
                pluralStringResource(Res.plurals.studio_issue_count, uiState.diagnostics.size, uiState.diagnostics.size),
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
                    pluralStringResource(
                        Res.plurals.studio_more_issues,
                        uiState.diagnostics.size - MAX_VISIBLE_DIAGNOSTICS,
                        uiState.diagnostics.size - MAX_VISIBLE_DIAGNOSTICS,
                    ),
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
                Text(if (uiState.isJsonPreviewVisible) stringResource(Res.string.studio_json_hide) else stringResource(Res.string.studio_json_preview))
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
                    text = preview + if (preview.length < uiState.rawJson.length) {
                        "\n" + stringResource(Res.string.studio_preview_truncated)
                    } else {
                        ""
                    },
                    modifier = Modifier.padding(12.dp),
                    style = AppTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
        AppButton(
            text = stringResource(Res.string.studio_export_json),
            modifier = Modifier.fillMaxWidth().testTag("opportunity-studio-export"),
            enabled = uiState.canExport,
            isLoading = uiState.isWorking,
            onClick = { onUiEvent(OpportunityStudioUiEvent.ExportDraft) },
        )
        Text(
            stringResource(Res.string.studio_export_caveat),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodySmall,
        )
        uiState.message?.let { message ->
            Surface(color = AppTheme.colors.alternative, shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(message.toText(), modifier = Modifier.weight(1f), style = AppTheme.typography.bodySmall)
                    TextButton(onClick = { onUiEvent(OpportunityStudioUiEvent.MessageHandled) }) {
                        Text(stringResource(Res.string.profile_dismiss))
                    }
                }
            }
        }
    }
}

@Composable
internal fun StudioPanel(
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
internal fun StudioTextField(
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
internal fun LabeledChoiceRow(
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
internal fun <T> EnumDropdown(
    label: String,
    value: T,
    values: List<T>,
    tag: String,
    labelFor: @Composable (T) -> String,
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
internal fun LoadingStudio() {
    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun UnsupportedStudio() {
    StudioPanel(title = stringResource(Res.string.studio_authoring_unavailable)) {
        Text(
            stringResource(Res.string.studio_device_unavailable),
            modifier = Modifier.testTag("opportunity-studio-unsupported"),
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
internal fun EmptyStudio(message: StudioNotice?) {
    StudioPanel(title = stringResource(Res.string.studio_unavailable)) {
        Text(message?.toText() ?: stringResource(Res.string.studio_pack_missing))
    }
}

internal fun opportunityStudioUsesTwoPane(maxWidth: Dp, fontScale: Float): Boolean = maxWidth >= PortaBreakpoints.workbench && fontScale < PortaBreakpoints.maxWideFontScale

internal fun ((OpportunityStudioUiEvent) -> Unit).metadata(
    field: OpportunityStudioMetadataField,
    value: String,
) = invoke(OpportunityStudioUiEvent.MetadataChanged(field, value))

internal fun ((OpportunityStudioUiEvent) -> Unit).ruleText(
    ruleIndex: Int,
    field: OpportunityStudioRuleTextField,
    value: String,
) = invoke(OpportunityStudioUiEvent.RuleTextChanged(ruleIndex, field, value))

@Composable
internal fun expectedValueLabel(kind: StudioFactValueKind): String = when (kind) {
    StudioFactValueKind.BOOLEAN -> stringResource(Res.string.studio_expected_boolean)
    StudioFactValueKind.NUMBER -> stringResource(Res.string.studio_expected_number)
    StudioFactValueKind.ENUM -> stringResource(Res.string.studio_expected_enum)
    StudioFactValueKind.ENUM_LIST -> stringResource(Res.string.studio_expected_values)
    StudioFactValueKind.TEXT -> stringResource(Res.string.studio_expected_text)
    StudioFactValueKind.DATE -> stringResource(Res.string.studio_expected_date)
    StudioFactValueKind.DURATION -> stringResource(Res.string.studio_expected_duration)
}

@Composable
internal fun expectedValueHelp(kind: StudioFactValueKind): String? = when (kind) {
    StudioFactValueKind.ENUM_LIST -> stringResource(Res.string.studio_expected_help_values)
    StudioFactValueKind.DATE -> stringResource(Res.string.studio_expected_help_date)
    StudioFactValueKind.DURATION -> stringResource(Res.string.studio_expected_help_duration)
    else -> null
}

private const val MAX_VISIBLE_DIAGNOSTICS = 8
private const val JSON_PREVIEW_LIMIT = 6_000
