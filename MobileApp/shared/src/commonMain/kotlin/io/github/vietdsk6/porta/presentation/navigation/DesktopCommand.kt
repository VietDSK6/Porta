package io.github.vietdsk6.porta.presentation.navigation

import androidx.compose.runtime.compositionLocalOf

/** Semantic desktop intents. The shared navigation layer decides how to fulfill each command. */
sealed interface DesktopCommand {
    data object NavigateRoute : DesktopCommand
    data object NavigateDoors : DesktopCommand
    data object NavigateNextMoves : DesktopCommand
    data object NavigateProfile : DesktopCommand
    data object NavigatePacks : DesktopCommand
    data object FocusSearch : DesktopCommand
    data object ImportPack : DesktopCommand
    data class ImportPackJson(val fileName: String, val rawJson: String) : DesktopCommand
    data object ExportPack : DesktopCommand
    data object ImportProfile : DesktopCommand
    data object ExportProfile : DesktopCommand
    data object ShowAbout : DesktopCommand
    data object ShowSources : DesktopCommand
}

/** A pending desktop action remains available until the destination screen handles it. */
data class DesktopCommandRequest(
    val command: DesktopCommand,
    val onHandled: () -> Unit,
)

val LocalDesktopCommand = compositionLocalOf<DesktopCommandRequest?> { null }
