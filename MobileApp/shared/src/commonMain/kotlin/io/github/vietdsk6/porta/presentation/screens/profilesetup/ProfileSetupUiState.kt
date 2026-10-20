package io.github.vietdsk6.porta.presentation.screens.profilesetup

class ProfileSetupUiState

sealed interface ProfileSetupUiEvent {
    data object OnClick : ProfileSetupUiEvent
}
