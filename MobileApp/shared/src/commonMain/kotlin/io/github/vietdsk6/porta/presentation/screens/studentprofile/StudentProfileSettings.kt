package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_ai_help
import io.github.vietdsk6.porta.generated.resources.profile_ai_key_absent
import io.github.vietdsk6.porta.generated.resources.profile_ai_key_delete
import io.github.vietdsk6.porta.generated.resources.profile_ai_key_label
import io.github.vietdsk6.porta.generated.resources.profile_ai_key_save
import io.github.vietdsk6.porta.generated.resources.profile_ai_key_saved
import io.github.vietdsk6.porta.generated.resources.profile_ai_title
import io.github.vietdsk6.porta.generated.resources.profile_clear_action
import io.github.vietdsk6.porta.generated.resources.profile_clear_body
import io.github.vietdsk6.porta.generated.resources.profile_clear_confirm_title
import io.github.vietdsk6.porta.generated.resources.profile_data_privacy
import io.github.vietdsk6.porta.generated.resources.profile_data_privacy_hint
import io.github.vietdsk6.porta.generated.resources.profile_dismiss
import io.github.vietdsk6.porta.generated.resources.profile_language_english
import io.github.vietdsk6.porta.generated.resources.profile_language_help
import io.github.vietdsk6.porta.generated.resources.profile_language_title
import io.github.vietdsk6.porta.generated.resources.profile_language_vietnamese
import io.github.vietdsk6.porta.generated.resources.profile_open
import io.github.vietdsk6.porta.generated.resources.profile_reset_action
import io.github.vietdsk6.porta.generated.resources.profile_reset_body
import io.github.vietdsk6.porta.generated.resources.profile_reset_confirm_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LanguageSettingsCard(
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-language"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(Res.string.profile_language_title),
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(Res.string.profile_language_help),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "en" to stringResource(Res.string.profile_language_english),
                    "vi" to stringResource(Res.string.profile_language_vietnamese),
                )
                    .forEach { (language, label) ->
                        FilterChip(
                            modifier = Modifier.testTag("student-profile-language-$language"),
                            selected = (uiState.selectedLanguage ?: "en") == language,
                            onClick = { onUiEvent(StudentProfileUiEvent.LanguageChanged(language)) },
                            enabled = !uiState.isWorking,
                            label = { Text(label) },
                        )
                    }
            }
        }
    }
}

@Composable
internal fun AiDraftSettingsCard(
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
) {
    var enteredKey by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-ai-drafting"),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(Res.string.profile_ai_title), style = AppTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(Res.string.profile_ai_help),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            Text(
                if (uiState.aiKeySaved) stringResource(Res.string.profile_ai_key_saved) else stringResource(Res.string.profile_ai_key_absent),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.secondary,
            )
            OutlinedTextField(
                value = enteredKey,
                onValueChange = { enteredKey = it },
                modifier = Modifier.fillMaxWidth().testTag("student-profile-ai-key-input"),
                label = { Text(stringResource(Res.string.profile_ai_key_label)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !uiState.isWorking,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    modifier = Modifier.testTag("student-profile-ai-key-save"),
                    enabled = !uiState.isWorking && enteredKey.isNotBlank(),
                    onClick = {
                        val key = enteredKey
                        enteredKey = ""
                        onUiEvent(StudentProfileUiEvent.AiKeySaveRequested(key))
                    },
                ) {
                    Text(stringResource(Res.string.profile_ai_key_save))
                }
                if (uiState.aiKeySaved) {
                    TextButton(
                        modifier = Modifier.testTag("student-profile-ai-key-delete"),
                        enabled = !uiState.isWorking,
                        onClick = { onUiEvent(StudentProfileUiEvent.AiKeyDeleteRequested) },
                    ) {
                        Text(stringResource(Res.string.profile_ai_key_delete))
                    }
                }
            }
        }
    }
}

@Composable
internal fun DataPrivacyEntry(
    isWorking: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student-profile-data-privacy")
            .clickable(enabled = !isWorking, role = Role.Button, onClick = onClick),
        color = AppTheme.colors.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.outline.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(Res.string.profile_data_privacy),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.text.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(Res.string.profile_data_privacy_hint),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.secondary,
                )
            }
            Text(
                text = stringResource(Res.string.profile_open),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
internal fun ProfileDataAction.confirmationTitle(): String = when (this) {
    ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> stringResource(Res.string.profile_clear_confirm_title)
    ProfileDataAction.RESET_ALL_LOCAL_DATA -> stringResource(Res.string.profile_reset_confirm_title)
}

@Composable
internal fun ProfileDataAction.confirmationBody(): String = when (this) {
    ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> stringResource(Res.string.profile_clear_body)
    ProfileDataAction.RESET_ALL_LOCAL_DATA -> stringResource(Res.string.profile_reset_body)
}

@Composable
internal fun ProfileDataAction.confirmationButton(): String = when (this) {
    ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> stringResource(Res.string.profile_clear_action)
    ProfileDataAction.RESET_ALL_LOCAL_DATA -> stringResource(Res.string.profile_reset_action)
}

@Composable
internal fun MessageNotice(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.defaultSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.primary,
            )
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.profile_dismiss)) }
        }
    }
}

internal fun List<ProfileFactUiState>.valueOf(key: FactKey): String? = firstOrNull { it.key == key }
    ?.value
    ?.takeIf(String::isNotBlank)

internal fun String?.initials(): String {
    if (isNullOrBlank()) return "P"
    return trim()
        .split(Regex("\\s+"))
        .take(2)
        .mapNotNull(String::firstOrNull)
        .joinToString(separator = "")
        .uppercase()
}

internal fun String.sentenceCase(): String = lowercase().replaceFirstChar(Char::uppercaseChar)
