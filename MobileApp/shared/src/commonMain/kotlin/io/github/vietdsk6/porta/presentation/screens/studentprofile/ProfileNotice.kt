package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_notice_ai_key_invalid
import io.github.vietdsk6.porta.generated.resources.profile_notice_ai_key_remove_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_ai_key_removed
import io.github.vietdsk6.porta.generated.resources.profile_notice_ai_key_save_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_ai_key_saved
import io.github.vietdsk6.porta.generated.resources.profile_notice_ai_settings_read_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_clear_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_cleared
import io.github.vietdsk6.porta.generated.resources.profile_notice_evidence_missing
import io.github.vietdsk6.porta.generated.resources.profile_notice_evidence_not_on_device
import io.github.vietdsk6.porta.generated.resources.profile_notice_evidence_open_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_export_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_exported
import io.github.vietdsk6.porta.generated.resources.profile_notice_fact_remove_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_fact_removed
import io.github.vietdsk6.porta.generated.resources.profile_notice_import_read_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_imported
import io.github.vietdsk6.porta.generated.resources.profile_notice_language_save_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_reset
import io.github.vietdsk6.porta.generated.resources.profile_notice_reset_failed
import io.github.vietdsk6.porta.generated.resources.profile_notice_unsupported_import
import org.jetbrains.compose.resources.stringResource

enum class ProfileNotice {
    IMPORT_READ_FAILED,
    FACT_REMOVED,
    FACT_REMOVE_FAILED,
    EVIDENCE_NOT_ON_DEVICE,
    EVIDENCE_OPEN_FAILED,
    EVIDENCE_MISSING,
    EXPORT_FAILED,
    EXPORTED,
    UNSUPPORTED_IMPORT,
    IMPORTED,
    CLEARED,
    RESET,
    CLEAR_FAILED,
    RESET_FAILED,
    AI_SETTINGS_READ_FAILED,
    AI_KEY_INVALID,
    AI_KEY_SAVED,
    AI_KEY_SAVE_FAILED,
    AI_KEY_REMOVED,
    AI_KEY_REMOVE_FAILED,
    LANGUAGE_SAVE_FAILED,
}

@Composable
internal fun ProfileNotice.toText(): String = stringResource(
    when (this) {
        ProfileNotice.IMPORT_READ_FAILED -> Res.string.profile_notice_import_read_failed
        ProfileNotice.FACT_REMOVED -> Res.string.profile_notice_fact_removed
        ProfileNotice.FACT_REMOVE_FAILED -> Res.string.profile_notice_fact_remove_failed
        ProfileNotice.EVIDENCE_NOT_ON_DEVICE -> Res.string.profile_notice_evidence_not_on_device
        ProfileNotice.EVIDENCE_OPEN_FAILED -> Res.string.profile_notice_evidence_open_failed
        ProfileNotice.EVIDENCE_MISSING -> Res.string.profile_notice_evidence_missing
        ProfileNotice.EXPORT_FAILED -> Res.string.profile_notice_export_failed
        ProfileNotice.EXPORTED -> Res.string.profile_notice_exported
        ProfileNotice.UNSUPPORTED_IMPORT -> Res.string.profile_notice_unsupported_import
        ProfileNotice.IMPORTED -> Res.string.profile_notice_imported
        ProfileNotice.CLEARED -> Res.string.profile_notice_cleared
        ProfileNotice.RESET -> Res.string.profile_notice_reset
        ProfileNotice.CLEAR_FAILED -> Res.string.profile_notice_clear_failed
        ProfileNotice.RESET_FAILED -> Res.string.profile_notice_reset_failed
        ProfileNotice.AI_SETTINGS_READ_FAILED -> Res.string.profile_notice_ai_settings_read_failed
        ProfileNotice.AI_KEY_INVALID -> Res.string.profile_notice_ai_key_invalid
        ProfileNotice.AI_KEY_SAVED -> Res.string.profile_notice_ai_key_saved
        ProfileNotice.AI_KEY_SAVE_FAILED -> Res.string.profile_notice_ai_key_save_failed
        ProfileNotice.AI_KEY_REMOVED -> Res.string.profile_notice_ai_key_removed
        ProfileNotice.AI_KEY_REMOVE_FAILED -> Res.string.profile_notice_ai_key_remove_failed
        ProfileNotice.LANGUAGE_SAVE_FAILED -> Res.string.profile_notice_language_save_failed
    },
)
