package io.github.vietdsk6.porta.presentation.screens.generationresult

import io.github.vietdsk6.porta.domain.model.generation.GenerationOutput

data class GenerationResultUiState(
    val generatedOutput: GenerationOutput? = null,
    val isLoading: Boolean = false,
    val isSaveToGalleryInProgress: Boolean = false,
    val isReportDialogVisible: Boolean = false,
)

sealed interface GenerationResultUiEvent {
    data object OnClickShare : GenerationResultUiEvent
    data object OnClickDownload : GenerationResultUiEvent
    data object OnClickReport : GenerationResultUiEvent
    data class OnSubmitReport(val reason: String) : GenerationResultUiEvent
    data object OnDismissReportDialog : GenerationResultUiEvent
}
