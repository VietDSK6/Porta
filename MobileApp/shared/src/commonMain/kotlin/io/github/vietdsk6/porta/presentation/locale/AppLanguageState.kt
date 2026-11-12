package io.github.vietdsk6.porta.presentation.locale

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Null follows the device language. The only supported overrides are en and vi. */
object AppLanguageState {
    var selected: String? by mutableStateOf(null)
        private set

    private val mutableChanges = MutableStateFlow<String?>(null)
    val changes: StateFlow<String?> = mutableChanges

    val effectiveLanguageTag: String get() = selected ?: LocalAppLocale.system

    fun select(languageTag: String?) {
        val normalized = languageTag?.takeIf { it == "en" || it == "vi" }
        selected = normalized
        mutableChanges.value = normalized
    }
}
