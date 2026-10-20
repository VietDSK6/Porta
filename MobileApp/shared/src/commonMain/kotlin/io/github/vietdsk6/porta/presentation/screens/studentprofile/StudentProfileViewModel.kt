package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StudentProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    fun onUiEvent(event: StudentProfileUiEvent) {
        when (event) {
            StudentProfileUiEvent.OnClick -> TODO()
        }
    }
}
