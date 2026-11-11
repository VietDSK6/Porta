package io.github.vietdsk6.porta

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.vietdsk6.porta.data.source.preferences.DesktopWindowGeometry
import io.github.vietdsk6.porta.data.source.preferences.DesktopWindowPreferences
import io.github.vietdsk6.porta.presentation.navigation.DesktopCommand
import io.github.vietdsk6.porta.root.App
import io.github.vietdsk6.porta.root.AppInitializer
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.URI
import kotlin.math.roundToInt

@OptIn(FlowPreview::class)
fun main() {
    System.setProperty("java.util.logging.SimpleFormatter.format", $$"%5$s%6$s%n")
    AppInitializer.initialize {}
    val initialGeometry = runBlocking { DesktopWindowPreferences.load() }
    val commandBus = DesktopCommandBus()

    application {
        val windowState = rememberWindowState(
            placement = if (initialGeometry.maximized) WindowPlacement.Maximized else WindowPlacement.Floating,
            position = initialGeometry.x?.let { x ->
                initialGeometry.y?.let { y -> WindowPosition(x.dp, y.dp) }
            } ?: WindowPosition.PlatformDefault,
            size = DpSize(initialGeometry.width.dp, initialGeometry.height.dp),
        )
        LaunchedEffect(windowState) {
            snapshotFlow { windowState.toGeometry() }
                .distinctUntilChanged()
                .debounce(300)
                .collect(DesktopWindowPreferences::save)
        }
        val closeWindow = {
            runBlocking { DesktopWindowPreferences.save(windowState.toGeometry()) }
            exitApplication()
        }
        Window(
            onCloseRequest = closeWindow,
            title = "Porta",
            state = windowState,
        ) {
            DesktopMenuBar(commandBus::send, onQuit = closeWindow)
            DesktopContent(commandBus)
        }
    }
}

private class DesktopCommandBus {
    private val channel = Channel<DesktopCommand>(Channel.BUFFERED)
    val commands = channel.receiveAsFlow()

    fun send(command: DesktopCommand) {
        channel.trySend(command)
    }
}

private fun WindowState.toGeometry(): DesktopWindowGeometry = DesktopWindowGeometry(
    width = size.width.value.roundToInt(),
    height = size.height.value.roundToInt(),
    x = position.takeIf(WindowPosition::isSpecified)?.x?.value?.roundToInt(),
    y = position.takeIf(WindowPosition::isSpecified)?.y?.value?.roundToInt(),
    maximized = placement == WindowPlacement.Maximized,
)

@Composable
private fun FrameWindowScope.DesktopMenuBar(
    onCommand: (DesktopCommand) -> Unit,
    onQuit: () -> Unit,
) {
    val isMac = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)
    fun shortcut(key: Key) = KeyShortcut(key, ctrl = !isMac, meta = isMac)

    MenuBar {
        Menu("File") {
            Item("Import pack…", shortcut = shortcut(Key.O), onClick = { onCommand(DesktopCommand.ImportPack) })
            Item("Export pack…", onClick = { onCommand(DesktopCommand.ExportPack) })
            Separator()
            Item("Import profile…", onClick = { onCommand(DesktopCommand.ImportProfile) })
            Item("Export profile…", shortcut = shortcut(Key.E), onClick = { onCommand(DesktopCommand.ExportProfile) })
            Separator()
            Item("Quit", onClick = onQuit)
        }
        Menu("View") {
            Item("Route", shortcut = shortcut(Key.One), onClick = { onCommand(DesktopCommand.NavigateRoute) })
            Item("Doors", shortcut = shortcut(Key.Two), onClick = { onCommand(DesktopCommand.NavigateDoors) })
            Item("Next moves", shortcut = shortcut(Key.Three), onClick = { onCommand(DesktopCommand.NavigateNextMoves) })
            Item("Profile", shortcut = shortcut(Key.Four), onClick = { onCommand(DesktopCommand.NavigateProfile) })
            Item("Packs", shortcut = shortcut(Key.Five), onClick = { onCommand(DesktopCommand.NavigatePacks) })
            Separator()
            Item("Find", shortcut = shortcut(Key.F), onClick = { onCommand(DesktopCommand.FocusSearch) })
        }
        Menu("Help") {
            Item("About Porta", onClick = { onCommand(DesktopCommand.ShowAbout) })
            Item("Sources", onClick = { onCommand(DesktopCommand.ShowSources) })
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DesktopContent(commandBus: DesktopCommandBus) {
    val dropTarget = remember(commandBus) {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val uris = (event.dragData() as? DragData.FilesList)?.readFiles() ?: return false
                val file = uris.singleOrNull()?.let { uri -> runCatching { File(URI(uri)) }.getOrNull() }
                    ?: return false
                if (!file.isFile || !file.extension.equals("json", ignoreCase = true) || file.length() > MAX_PACK_JSON_BYTES) {
                    return false
                }
                val json = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return false
                commandBus.send(DesktopCommand.ImportPackJson(file.name, json))
                return true
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().dragAndDropTarget(
            shouldStartDragAndDrop = { event -> event.dragData() is DragData.FilesList },
            target = dropTarget,
        ),
    ) {
        App(desktopCommands = commandBus.commands)
    }
}

private const val MAX_PACK_JSON_BYTES = 10L * 1024 * 1024
