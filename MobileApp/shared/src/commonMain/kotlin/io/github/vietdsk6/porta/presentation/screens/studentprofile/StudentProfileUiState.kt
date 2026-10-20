package io.github.vietdsk6.porta.presentation.screens.studentprofile

class StudentProfileUiState

sealed interface StudentProfileUiEvent {
    data object OnClick : StudentProfileUiEvent
}
