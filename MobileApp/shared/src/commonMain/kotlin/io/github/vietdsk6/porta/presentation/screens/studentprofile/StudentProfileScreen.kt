package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ButtonStyle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.data_privacy_keep_data
import io.github.vietdsk6.porta.generated.resources.profile_build
import io.github.vietdsk6.porta.generated.resources.profile_cancel
import io.github.vietdsk6.porta.generated.resources.profile_edit
import io.github.vietdsk6.porta.generated.resources.profile_explore_pack
import io.github.vietdsk6.porta.generated.resources.profile_remove_confirm
import io.github.vietdsk6.porta.generated.resources.profile_remove_fact
import io.github.vietdsk6.porta.generated.resources.profile_remove_question
import io.github.vietdsk6.porta.generated.resources.profile_saved_notice
import io.github.vietdsk6.porta.generated.resources.profile_start_body
import io.github.vietdsk6.porta.generated.resources.profile_start_eyebrow
import io.github.vietdsk6.porta.generated.resources.profile_start_title
import io.github.vietdsk6.porta.generated.resources.profile_title
import io.github.vietdsk6.porta.presentation.navigation.DesktopCommand
import io.github.vietdsk6.porta.presentation.navigation.LocalDesktopCommand
import org.jetbrains.compose.resources.stringResource

@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: StudentProfileViewModel,
    onEditProfile: () -> Unit,
    onOpenPackInspector: () -> Unit,
    onOpenDataPrivacy: () -> Unit,
    saveNotice: Boolean = false,
    onSaveNoticeDismissed: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val desktopRequest = LocalDesktopCommand.current
    LaunchedEffect(desktopRequest?.command) {
        if (desktopRequest?.command == DesktopCommand.ExportProfile) {
            viewModel.onUiEvent(StudentProfileUiEvent.ExportMetadata)
            desktopRequest.onHandled()
        }
    }

    StudentProfileScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onEditProfile = onEditProfile,
        onOpenPackInspector = onOpenPackInspector,
        onOpenDataPrivacy = onOpenDataPrivacy,
        saveNotice = saveNotice,
        onSaveNoticeDismissed = onSaveNoticeDismissed,
    )
}

@Composable
fun StudentProfileScreen(
    modifier: Modifier = Modifier,
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
    onEditProfile: () -> Unit,
    onOpenPackInspector: () -> Unit,
    onOpenDataPrivacy: () -> Unit = {},
    saveNotice: Boolean = false,
    onSaveNoticeDismissed: () -> Unit = {},
) {
    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = true,
        title = stringResource(Res.string.profile_title),
        includeBottomInsets = false,
        toolbarExtraContent = {
            if (uiState.hasRealProfile) {
                TextButton(
                    modifier = Modifier.testTag("student-profile-edit"),
                    enabled = !uiState.isWorking,
                    onClick = onEditProfile,
                ) {
                    Text(
                        text = stringResource(Res.string.profile_edit),
                        color = AppTheme.colors.primary.copy(alpha = if (uiState.isWorking) 0.5f else 1f),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            when {
                uiState.isLoading -> LoadingProfile()

                !uiState.hasRealProfile -> {
                    EmptyProfile(
                        onEditProfile = onEditProfile,
                        onOpenPackInspector = onOpenPackInspector,
                    )
                    if (uiState.aiDraftSettingsAvailable) {
                        AiDraftSettingsCard(uiState = uiState, onUiEvent = onUiEvent)
                    }
                }

                else -> ProfileContent(
                    uiState = uiState,
                    onUiEvent = onUiEvent,
                    onEditProfile = onEditProfile,
                    saveNotice = saveNotice,
                    onSaveNoticeDismissed = onSaveNoticeDismissed,
                )
            }

            if (!uiState.isLoading) {
                if (uiState.languageSettingsAvailable) {
                    LanguageSettingsCard(uiState = uiState, onUiEvent = onUiEvent)
                }
                DataPrivacyEntry(
                    isWorking = uiState.isWorking,
                    onClick = onOpenDataPrivacy,
                )
            }

            uiState.message?.let { message ->
                MessageNotice(
                    message = message.toText(),
                    onDismiss = { onUiEvent(StudentProfileUiEvent.MessageHandled) },
                )
            }
        }
    }

    uiState.factPendingDelete?.let { fact ->
        AlertDialog(
            onDismissRequest = { onUiEvent(StudentProfileUiEvent.DeleteFactCancelled) },
            title = { Text(stringResource(Res.string.profile_remove_question, fact.key.localizedLabel())) },
            text = {
                Text(stringResource(Res.string.profile_remove_confirm))
            },
            confirmButton = {
                TextButton(onClick = { onUiEvent(StudentProfileUiEvent.DeleteFactConfirmed) }) {
                    Text(stringResource(Res.string.profile_remove_fact))
                }
            },
            dismissButton = {
                TextButton(onClick = { onUiEvent(StudentProfileUiEvent.DeleteFactCancelled) }) {
                    Text(stringResource(Res.string.profile_cancel))
                }
            },
        )
    }
    uiState.pendingDataAction?.let { action ->
        AlertDialog(
            modifier = Modifier.testTag("student-profile-data-confirmation"),
            onDismissRequest = { onUiEvent(StudentProfileUiEvent.DataActionCancelled) },
            title = { Text(action.confirmationTitle()) },
            text = { Text(action.confirmationBody()) },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("student-profile-data-confirm"),
                    onClick = { onUiEvent(StudentProfileUiEvent.DataActionConfirmed) },
                ) { Text(action.confirmationButton()) }
            },
            dismissButton = {
                TextButton(onClick = { onUiEvent(StudentProfileUiEvent.DataActionCancelled) }) {
                    Text(stringResource(Res.string.data_privacy_keep_data))
                }
            },
        )
    }
}

@Composable
internal fun LoadingProfile() {
    Column(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun EmptyProfile(
    onEditProfile: () -> Unit,
    onOpenPackInspector: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("student-profile-empty"),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.alternative),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileAvatar(monogram = "+")
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.profile_start_eyebrow),
                        color = AppTheme.colors.primary,
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = stringResource(Res.string.profile_start_title),
                        style = AppTheme.typography.h4,
                        color = AppTheme.colors.text.primary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Text(
                text = stringResource(Res.string.profile_start_body),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                text = stringResource(Res.string.profile_build),
                modifier = Modifier.fillMaxWidth().testTag("student-profile-build"),
                onClick = onEditProfile,
            )
            AppButton(
                text = stringResource(Res.string.profile_explore_pack),
                modifier = Modifier.fillMaxWidth().testTag("student-profile-explore-pack"),
                style = ButtonStyle.ALTERNATIVE,
                onClick = onOpenPackInspector,
            )
        }
    }
}

@Composable
internal fun ProfileContent(
    uiState: StudentProfileUiState,
    onUiEvent: (StudentProfileUiEvent) -> Unit,
    onEditProfile: () -> Unit,
    saveNotice: Boolean,
    onSaveNoticeDismissed: () -> Unit,
) {
    if (uiState.isDemoProfile) {
        DemoProfileNotice(
            isWorking = uiState.isWorking,
            onRemove = {
                onUiEvent(StudentProfileUiEvent.DataActionRequested(ProfileDataAction.CLEAR_PROFILE_AND_PLANS))
            },
        )
    }
    ProfileIdentity(uiState = uiState)

    if (uiState.aiDraftSettingsAvailable) {
        AiDraftSettingsCard(uiState = uiState, onUiEvent = onUiEvent)
    }

    if (saveNotice) {
        MessageNotice(
            message = stringResource(Res.string.profile_saved_notice),
            onDismiss = onSaveNoticeDismissed,
        )
    }

    if (uiState.uncheckedFacts.isNotEmpty()) {
        UncheckedFactsNotice(
            facts = uiState.uncheckedFacts,
            onEditProfile = onEditProfile,
        )
    }

    if (uiState.isSampleCatalog) {
        DemoPackNotice()
    }

    uiState.sections.forEach { section ->
        FactSection(
            section = section,
            isDemoProfile = uiState.isDemoProfile,
            expandedFactKey = uiState.expandedFactKey,
            onUiEvent = onUiEvent,
        )
    }
}
