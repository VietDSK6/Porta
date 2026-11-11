package io.github.vietdsk6.porta.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.vietdsk6.porta.designsystem.AllComponentsGallery
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.presentation.navigation.AppNavigation
import io.github.vietdsk6.porta.presentation.navigation.DesktopCommand
import io.github.vietdsk6.porta.util.UiMessage
import io.github.vietdsk6.porta.util.extensions.ObserveFlowAsEvent
import io.github.vietdsk6.porta.util.logging.AppLogger
import io.github.vietdsk6.porta.util.logging.logAppOpened
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.compose.koinInject

/** Root composable — the single entry point every platform (Android/iOS/Desktop/Web) renders. */
@Composable
fun App(desktopCommands: Flow<DesktopCommand>? = null) {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Set this to true for showing app, or false for ui components gallery
            val isPreviewComponentsMode = false
            if (isPreviewComponentsMode) {
                AllComponentsGallery()
            } else {
                AppScaffold(desktopCommands)
            }
        }
    }
}

@Composable
private fun AppScaffold(desktopCommands: Flow<DesktopCommand>?) {
    val snackbarHostState = remember { SnackbarHostState() }
    var uiMessage by remember { mutableStateOf<UiMessage?>(null) }
    RefreshCurrentDateEffect(koinInject())

    ObserveFlowAsEvent(AppGlobalUiState.uiMessageFlow) { uiMessage = it }
    LaunchedEffect(Unit) {
        AppLogger.logAppOpened()
    }

    uiMessage?.value?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message = message)
            uiMessage = null
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
        AppNavigation(desktopCommands)
    }
}

@Composable
private fun RefreshCurrentDateEffect(currentLocalDateProvider: CurrentLocalDateProvider) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentLocalDateProvider) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) currentLocalDateProvider.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(currentLocalDateProvider) {
        currentLocalDateProvider.refresh()
        while (true) {
            delay(DATE_REFRESH_POLL_MILLIS)
            currentLocalDateProvider.refresh()
        }
    }
}

private const val DATE_REFRESH_POLL_MILLIS = 60_000L

/**
 * App-wide bus for one-off UI messages (snackbars). Lets any layer surface a transient message
 * without holding a screen reference — [AppScaffold] collects [uiMessageFlow] and shows it.
 */
object AppGlobalUiState {
    private val uiMessageChannel = Channel<UiMessage>(Channel.BUFFERED)
    val uiMessageFlow = uiMessageChannel.receiveAsFlow()

    fun showUiMessage(uiMessage: UiMessage) {
        uiMessageChannel.trySend(uiMessage)
    }
}
