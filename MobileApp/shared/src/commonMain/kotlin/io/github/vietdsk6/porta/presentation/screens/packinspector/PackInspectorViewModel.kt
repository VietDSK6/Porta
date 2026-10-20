package io.github.vietdsk6.porta.presentation.screens.packinspector

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PackInspectorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PackInspectorUiState())
    val uiState: StateFlow<PackInspectorUiState> = _uiState.asStateFlow()

    fun onUiEvent(event: PackInspectorUiEvent) {
        when (event) {
            PackInspectorUiEvent.OnClick -> TODO()
        }
    }
}
