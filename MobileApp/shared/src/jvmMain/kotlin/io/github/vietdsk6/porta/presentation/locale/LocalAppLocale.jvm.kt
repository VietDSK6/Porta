package io.github.vietdsk6.porta.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

actual object LocalAppLocale {
    private val systemLocale: Locale by lazy { Locale.getDefault() }
    actual val system: String get() = systemLocale.toLanguageTag()
    private val locale = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }

    actual val current: String
        @Composable get() = locale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val chosen = value?.let(Locale::forLanguageTag) ?: systemLocale
        Locale.setDefault(chosen)
        return locale.provides(chosen.toLanguageTag())
    }
}
