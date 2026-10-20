package io.github.vietdsk6.porta.presentation.screens.gallery

import io.github.vietdsk6.porta.domain.model.generation.GenerationOutput

data class GalleryUiState(
    val generations: List<GenerationOutput> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface GalleryUiEvent {
    data class OnClickItem(val item: GenerationOutput) : GalleryUiEvent
    data object OnClickGenerate : GalleryUiEvent
}
