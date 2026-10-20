package io.github.vietdsk6.porta.presentation.screens.packinspector

class PackInspectorUiState

sealed interface PackInspectorUiEvent {
    data object OnClick : PackInspectorUiEvent
}
