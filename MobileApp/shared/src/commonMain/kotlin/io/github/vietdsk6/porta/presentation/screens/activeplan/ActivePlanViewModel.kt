package io.github.vietdsk6.porta.presentation.screens.activeplan

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ActivePlanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ActivePlanUiState())
    val uiState: StateFlow<ActivePlanUiState> = _uiState.asStateFlow()

    fun onUiEvent(event: ActivePlanUiEvent) {
        when (event) {
            ActivePlanUiEvent.OnClick -> TODO()
        }
    }
}
