package io.github.vietdsk6.porta.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale

/** Browser locale override is deferred until the Web target is in scope. */
actual object LocalAppLocale {
    actual val system: String get() = "en"
    private val locale = staticCompositionLocalOf { Locale.current.toString() }

    actual val current: String
        @Composable get() = locale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> = locale.provides(value ?: Locale.current.toString())
}
