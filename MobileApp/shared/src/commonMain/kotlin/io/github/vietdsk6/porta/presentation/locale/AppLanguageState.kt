package io.github.vietdsk6.porta.presentation.locale

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** English is the first-run default; Vietnamese is an explicit in-app choice. */
object AppLanguageState {
    var selected: String? by mutableStateOf("en")
        private set

    private val mutableChanges = MutableStateFlow<String?>("en")
    val changes: StateFlow<String?> = mutableChanges

    val effectiveLanguageTag: String get() = selected ?: "en"

    fun select(languageTag: String?) {
        val normalized = languageTag?.takeIf { it == "en" || it == "vi" } ?: "en"
        selected = normalized
        mutableChanges.value = normalized
    }
}
