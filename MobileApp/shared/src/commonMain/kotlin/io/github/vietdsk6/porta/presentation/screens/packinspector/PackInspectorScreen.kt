package io.github.vietdsk6.porta.presentation.screens.packinspector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
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
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.pack_studio_eyebrow
import io.github.vietdsk6.porta.generated.resources.pack_studio_hint
import io.github.vietdsk6.porta.generated.resources.pack_studio_hint_full
import io.github.vietdsk6.porta.generated.resources.pack_studio_open
import io.github.vietdsk6.porta.generated.resources.pack_studio_title
import io.github.vietdsk6.porta.generated.resources.pack_title
import io.github.vietdsk6.porta.presentation.navigation.DesktopCommand
import io.github.vietdsk6.porta.presentation.navigation.LocalDesktopCommand
import io.github.vietdsk6.porta.util.PlatformCapabilities
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun PackInspectorScreen(
    viewModel: PackInspectorViewModel,
    onNavigateBack: () -> Unit,
    onOpenOpportunityStudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val packPicker = rememberFilePickerLauncher(
        type = FileKitType.File("json"),
        mode = FileKitMode.Single,
    ) { file ->
        file ?: return@rememberFilePickerLauncher
        coroutineScope.launch {
            runCatching { file.readString() }.fold(
                onSuccess = { rawJson ->
                    viewModel.onUiEvent(PackInspectorUiEvent.ImportPack(file.name, rawJson))
                },
                onFailure = {
                    viewModel.onUiEvent(PackInspectorUiEvent.ImportReadFailed)
                },
            )
        }
    }
    val desktopRequest = LocalDesktopCommand.current
    LaunchedEffect(desktopRequest?.command) {
        when (val command = desktopRequest?.command) {
            DesktopCommand.ImportPack -> packPicker.launch()

            is DesktopCommand.ImportPackJson -> viewModel.onUiEvent(
                PackInspectorUiEvent.ImportPack(command.fileName, command.rawJson),
            )

            DesktopCommand.ExportPack -> viewModel.onUiEvent(PackInspectorUiEvent.ExportActivePack)

            else -> return@LaunchedEffect
        }
        desktopRequest?.onHandled()
    }

    PackInspectorScreen(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigateBack = onNavigateBack,
        onImportPack = packPicker::launch,
        onOpenSource = uriHandler::openUri,
        supportsOpportunityStudio = koinInject<PlatformCapabilities>().supportsStudio,
        onOpenOpportunityStudio = onOpenOpportunityStudio,
    )
}

@Composable
fun PackInspectorScreen(
    uiState: PackInspectorUiState,
    onUiEvent: (PackInspectorUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onImportPack: () -> Unit,
    onOpenSource: (String) -> Unit,
    supportsOpportunityStudio: Boolean = false,
    onOpenOpportunityStudio: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ScreenWithToolbar(
        modifier = modifier.testTag("pack-inspector-screen"),
        isScrollableContent = true,
        title = stringResource(Res.string.pack_title),
        includeBottomInsets = true,
        navigationIcon = UiRes.drawable.ic_back,
        onNavigationIconClick = onNavigateBack,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
        ) {
            InspectorIntro()

            if (supportsOpportunityStudio) {
                OpportunityStudioEntry(onOpenOpportunityStudio)
            }

            when {
                uiState.isLoading -> LoadingPack()

                uiState.activePack == null -> EmptyPack(
                    isWorking = uiState.isWorking,
                    onImportPack = onImportPack,
                    onRestore = { onUiEvent(PackInspectorUiEvent.RestoreBundledRequested) },
                )

                else -> ActivePackContent(
                    uiState = uiState,
                    onUiEvent = onUiEvent,
                    onImportPack = onImportPack,
                    onOpenSource = onOpenSource,
                )
            }

            if (uiState.diagnostics.isNotEmpty()) {
                DiagnosticsCard(
                    diagnostics = uiState.diagnostics,
                    isWorking = uiState.isWorking,
                    onExport = { onUiEvent(PackInspectorUiEvent.ExportDiagnostics) },
                )
            }

            uiState.message?.let { message ->
                MessageNotice(
                    message = message.toText(),
                    onDismiss = { onUiEvent(PackInspectorUiEvent.MessageHandled) },
                )
            }
        }
    }

    uiState.pendingUpdate?.let { pending ->
        PackUpdatePreviewDialog(
            pending = pending,
            isWorking = uiState.isWorking,
            onConfirm = { onUiEvent(PackInspectorUiEvent.ImportPreviewConfirmed) },
            onCancel = { onUiEvent(PackInspectorUiEvent.ImportPreviewCancelled) },
        )
    }
}

@Composable
private fun OpportunityStudioEntry(onOpenOpportunityStudio: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
        border = BorderStroke(1.dp, AppTheme.colors.primary.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.cardContentSpacing),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(Res.string.pack_studio_eyebrow),
                color = AppTheme.colors.primary,
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
            )
            Text(
                text = stringResource(Res.string.pack_studio_title),
                style = AppTheme.typography.h5,
                color = AppTheme.colors.text.primary,
            )
            Text(
                text = stringResource(Res.string.pack_studio_hint_full),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text.secondary,
            )
            AppButton(
                modifier = Modifier.fillMaxWidth().testTag("pack-inspector-open-studio"),
                text = stringResource(Res.string.pack_studio_open),
                onClick = onOpenOpportunityStudio,
            )
        }
    }
}
