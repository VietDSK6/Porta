package io.github.vietdsk6.porta.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults

actual object LocalAppLocale {
    private val systemLocale = NSLocale.preferredLanguages.first() as String
    actual val system: String get() = systemLocale
    private val locale = staticCompositionLocalOf { systemLocale }

    actual val current: String
        @Composable get() = locale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if (value == null) {
            NSUserDefaults.standardUserDefaults.removeObjectForKey("AppleLanguages")
        } else {
            NSUserDefaults.standardUserDefaults.setObject(listOf(value), "AppleLanguages")
        }
        return locale.provides(value ?: systemLocale)
    }
}
