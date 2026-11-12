package io.github.vietdsk6.porta.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key

/** Platform resource environments use this locale when the user chooses an in-app language. */
expect object LocalAppLocale {
    val system: String

    val current: String
        @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

@Composable
fun AppLocaleEnvironment(content: @Composable () -> Unit) {
    val selected = AppLanguageState.selected
    CompositionLocalProvider(LocalAppLocale provides selected) {
        key(selected) { content() }
    }
}
