package io.github.vietdsk6.porta.presentation.screens.activeplan

class ActivePlanUiState

sealed interface ActivePlanUiEvent {
    data object OnClick : ActivePlanUiEvent
}
