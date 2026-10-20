package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileSetupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun onUiEvent(event: ProfileSetupUiEvent) {
        when (event) {
            ProfileSetupUiEvent.OnClick -> TODO()
        }
    }
}
