package io.github.vietdsk6.porta.presentation.locale

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Null follows the device language. The only supported overrides are en and vi. */
object AppLanguageState {
    var selected: String? by mutableStateOf(null)
        private set

    val effectiveLanguageTag: String get() = selected ?: LocalAppLocale.system

    fun select(languageTag: String?) {
        selected = languageTag?.takeIf { it == "en" || it == "vi" }
    }
}
