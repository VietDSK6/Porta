package io.github.vietdsk6.porta.presentation.locale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private val systemLocale: Locale by lazy { Locale.getDefault() }
    actual val system: String get() = systemLocale.toLanguageTag()

    actual val current: String
        @Composable get() = LocalConfiguration.current.locales[0].toLanguageTag()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val locale = value?.let(Locale::forLanguageTag) ?: systemLocale
        val configuration = Configuration(LocalConfiguration.current).apply { setLocale(locale) }
        Locale.setDefault(locale)
        val resources = LocalContext.current.resources
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}
