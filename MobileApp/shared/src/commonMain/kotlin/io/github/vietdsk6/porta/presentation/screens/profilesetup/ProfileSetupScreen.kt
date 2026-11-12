package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_setup_header
import io.github.vietdsk6.porta.generated.resources.profile_setup_more_hint
import io.github.vietdsk6.porta.generated.resources.profile_setup_more_title
import io.github.vietdsk6.porta.generated.resources.profile_setup_request_body
import io.github.vietdsk6.porta.generated.resources.profile_setup_request_caveat
import io.github.vietdsk6.porta.generated.resources.profile_setup_request_eyebrow
import io.github.vietdsk6.porta.generated.resources.profile_setup_save
import io.github.vietdsk6.porta.generated.resources.profile_setup_title
import org.jetbrains.compose.resources.stringResource

private val profileFormMaxWidth = 620.dp

@Composable
fun ProfileSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel,
    onCompleted: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenDataPrivacy: () -> Unit = {},
    requestedFactKeyName: String? = null,
    requestContext: String? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val requestedFactKey = requestedFactKeyName?.let { name ->
        FactKey.entries.firstOrNull { it.name == name }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onCompleted()
            viewModel.onUiEvent(ProfileSetupUiEvent.SaveHandled)
        }
    }

    ProfileSetupScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onOpenDataPrivacy = onOpenDataPrivacy,
        requestedFactKey = requestedFactKey,
        requestContext = requestContext,
    )
}

@Composable
fun ProfileSetupScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileSetupUiState,
    onUiEvent: (ProfileSetupUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenDataPrivacy: () -> Unit = {},
    requestedFactKey: FactKey? = null,
    requestContext: String? = null,
) {
    var expandedSectionName by rememberSaveable(requestedFactKey?.name) {
        mutableStateOf(requestedFactKey?.setupSection()?.name)
    }
    var hasEdited by rememberSaveable { mutableStateOf(false) }
    val onEditEvent: (ProfileSetupUiEvent) -> Unit = { event ->
        hasEdited = true
        onUiEvent(event)
    }

    ScreenWithToolbar(
        modifier = modifier,
        isScrollableContent = true,
        title = stringResource(Res.string.profile_setup_title),
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = profileFormMaxWidth)
                    .testTag("profile-setup-form"),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
            ) {
                ProfileSetupHeader()
                requestedFactKey?.let { factKey ->
                    RequestedFactNotice(
                        factKey = factKey,
                        requestContext = requestContext,
                    )
                }
                EssentialsSection(uiState = uiState, onUiEvent = onEditEvent)
                AdditionalFactsIntro()
                AcademicRecordSection(
                    uiState = uiState,
                    onUiEvent = onEditEvent,
                    isExpanded = expandedSectionName == ProfileSetupSection.ACADEMIC.name,
                    onToggle = {
                        expandedSectionName = ProfileSetupSection.ACADEMIC.name
                            .takeUnless { it == expandedSectionName }
                    },
                )
                ExperienceEvidenceSection(
                    uiState = uiState,
                    onUiEvent = onEditEvent,
                    isExpanded = expandedSectionName == ProfileSetupSection.EXPERIENCE.name,
                    onToggle = {
                        expandedSectionName = ProfileSetupSection.EXPERIENCE.name
                            .takeUnless { it == expandedSectionName }
                    },
                )
                ContextSection(
                    uiState = uiState,
                    onUiEvent = onEditEvent,
                    isExpanded = expandedSectionName == ProfileSetupSection.CONTEXT.name,
                    onToggle = {
                        expandedSectionName = ProfileSetupSection.CONTEXT.name
                            .takeUnless { it == expandedSectionName }
                    },
                )
                LocalDataNotice(onOpenDataPrivacy)

                uiState.error?.let { error ->
                    Surface(
                        color = AppTheme.colors.status.errorContainer,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text = profileSetupMessage(error, uiState.gpaScale).orEmpty(),
                            modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
                            color = AppTheme.colors.status.error,
                        )
                    }
                }

                AppButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile-setup-save"),
                    text = stringResource(Res.string.profile_setup_save),
                    enabled = uiState.canSave,
                    isLoading = uiState.isSaving,
                    onClick = { onUiEvent(ProfileSetupUiEvent.Save) },
                )
                if (!uiState.canSave && !uiState.isSaving) {
                    Text(
                        text = profileSetupMessage(uiState.saveDisabledReason, uiState.gpaScale).orEmpty(),
                        modifier = Modifier.testTag("profile-setup-save-reason"),
                        color = if (hasEdited) AppTheme.colors.status.error else AppTheme.colors.text.secondary,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestedFactNotice(
    factKey: FactKey,
    requestContext: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("profile-requested-${factKey.name.lowercase()}"),
        color = AppTheme.colors.alternative,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(Res.string.profile_setup_request_eyebrow),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
            )
            Text(
                text = stringResource(Res.string.profile_setup_request_body, factKey.profileFieldLabel()),
                color = AppTheme.colors.text.primary,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            requestContext?.takeIf(String::isNotBlank)?.let { context ->
                Text(
                    text = context,
                    color = AppTheme.colors.text.secondary,
                    style = AppTheme.typography.bodyMedium,
                )
            }
            Text(
                text = stringResource(Res.string.profile_setup_request_caveat),
                color = AppTheme.colors.text.secondary,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AdditionalFactsIntro() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(Res.string.profile_setup_more_title),
            style = AppTheme.typography.h5,
            color = AppTheme.colors.text.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(Res.string.profile_setup_more_hint),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text.secondary,
        )
    }
}

@Composable
private fun ProfileSetupHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(Res.string.profile_setup_header),
            color = AppTheme.colors.text.secondary,
            style = AppTheme.typography.bodyLarge,
        )
    }
}
