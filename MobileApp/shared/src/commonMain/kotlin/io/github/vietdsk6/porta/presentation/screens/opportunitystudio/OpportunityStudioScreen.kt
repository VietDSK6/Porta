package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vietdsk6.porta.designsystem.components.ScreenWithToolbar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_back
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.studio_eyebrow
import io.github.vietdsk6.porta.generated.resources.studio_intro_body
import io.github.vietdsk6.porta.generated.resources.studio_intro_title
import io.github.vietdsk6.porta.generated.resources.studio_pack_version_transition
import io.github.vietdsk6.porta.generated.resources.studio_title
import org.jetbrains.compose.resources.stringResource

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
        title = stringResource(Res.string.studio_title),
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
internal fun StudioContent(
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
internal fun StudioIntro(uiState: OpportunityStudioUiState) {
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
                text = stringResource(Res.string.studio_eyebrow),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(stringResource(Res.string.studio_intro_title), style = AppTheme.typography.h4)
            Text(
                stringResource(Res.string.studio_intro_body, uiState.basePackName),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            if (uiState.basePackVersion != null && uiState.nextPackVersion != null) {
                Text(
                    stringResource(Res.string.studio_pack_version_transition, uiState.basePackVersion, uiState.nextPackVersion),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
