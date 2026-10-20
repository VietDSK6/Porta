package io.github.vietdsk6.porta.designsystem.components.addorchosefilecontainer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vietdsk6.porta.designsystem.components.AppButton
import io.github.vietdsk6.porta.designsystem.components.ButtonSize
import io.github.vietdsk6.porta.designsystem.components.TipsUiComponent
import io.github.vietdsk6.porta.designsystem.components.modals.AppModalBottomSheet
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.btn_choose_from_gallery
import io.github.vietdsk6.porta.designsystem.generated.resources.btn_record_video
import io.github.vietdsk6.porta.designsystem.generated.resources.btn_take_photo
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_camera
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_file_image
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_file_video
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_record_video
import io.github.vietdsk6.porta.designsystem.generated.resources.select_photo_source_modal_title
import io.github.vietdsk6.porta.designsystem.generated.resources.select_video_source_modal_title
import io.github.vietdsk6.porta.designsystem.generated.resources.tip_text_for_better_result
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.designsystem.util.UiText
import org.jetbrains.compose.resources.stringResource

@Composable
fun FileSourceModal(
    type: FileUiType,
    onDismiss: () -> Unit,
    onClickCaptureOrRecord: () -> Unit,
    onClickSelectFromGallery: () -> Unit,
    isTipsTextVisible: Boolean = true,
    isCaptureSupported: Boolean = true,
    hintUiText: UiText? = null,
) {
    val modalTitle =
        when (type) {
            FileUiType.IMAGE -> stringResource(UiRes.string.select_photo_source_modal_title)
            FileUiType.VIDEO -> stringResource(UiRes.string.select_video_source_modal_title)
        }

    val btnRecordOrCaptureNowText =
        when (type) {
            FileUiType.IMAGE -> stringResource(UiRes.string.btn_take_photo)
            FileUiType.VIDEO -> stringResource(UiRes.string.btn_record_video)
        }

    val btnRecordOrCaptureNowIcon =
        when (type) {
            FileUiType.IMAGE -> UiRes.drawable.ic_camera
            FileUiType.VIDEO -> UiRes.drawable.ic_record_video
        }

    val btnSelectFromGalleryIcon =
        when (type) {
            FileUiType.IMAGE -> UiRes.drawable.ic_file_image
            FileUiType.VIDEO -> UiRes.drawable.ic_file_video
        }

    AppModalBottomSheet(
        hideButtons = true,
        title = modalTitle,
        isDismissable = true,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
        ) {
            if (isCaptureSupported) {
                AppButton(
                    size = ButtonSize.SMALL,
                    modifier = Modifier.fillMaxWidth(),
                    text = btnRecordOrCaptureNowText,
                    startIcon = btnRecordOrCaptureNowIcon,
                    onClick = onClickCaptureOrRecord,
                )
            }

            AppButton(
                size = ButtonSize.SMALL,
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(UiRes.string.btn_choose_from_gallery),
                startIcon = btnSelectFromGalleryIcon,
                onClick = onClickSelectFromGallery,
            )

            if (isTipsTextVisible) {
                TipsUiComponent(
                    text =
                    hintUiText?.value
                        ?: stringResource(UiRes.string.tip_text_for_better_result),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
