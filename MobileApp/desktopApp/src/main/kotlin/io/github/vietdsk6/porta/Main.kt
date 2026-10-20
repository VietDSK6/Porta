package io.github.vietdsk6.porta

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vietdsk6.porta.root.App
import io.github.vietdsk6.porta.root.AppInitializer

fun main() {
    System.setProperty("java.util.logging.SimpleFormatter.format", $$"%5$s%6$s%n")

    application {
        AppInitializer.initialize {}
        Window(
            onCloseRequest = ::exitApplication,
            title = "Porta",
        ) {
            App()
        }
    }
}
