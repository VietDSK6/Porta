package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.studio_notice_ai_rule_locked
import io.github.vietdsk6.porta.generated.resources.studio_notice_draft_exhausted
import io.github.vietdsk6.porta.generated.resources.studio_notice_draft_failed
import io.github.vietdsk6.porta.generated.resources.studio_notice_draft_unavailable
import io.github.vietdsk6.porta.generated.resources.studio_notice_export_unavailable
import io.github.vietdsk6.porta.generated.resources.studio_notice_exported
import io.github.vietdsk6.porta.generated.resources.studio_notice_load_failed
import io.github.vietdsk6.porta.generated.resources.studio_notice_pack_missing
import io.github.vietdsk6.porta.generated.resources.studio_notice_quote_required
import io.github.vietdsk6.porta.generated.resources.studio_notice_rule_added
import io.github.vietdsk6.porta.generated.resources.studio_notice_save_failed
import io.github.vietdsk6.porta.generated.resources.studio_notice_source_changed
import io.github.vietdsk6.porta.generated.resources.studio_notice_validation_required
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StudioNotice.toText(): String = stringResource(
    when (this) {
        StudioNotice.SOURCE_CHANGED -> Res.string.studio_notice_source_changed
        StudioNotice.DRAFT_UNAVAILABLE -> Res.string.studio_notice_draft_unavailable
        StudioNotice.NO_RULES_SUGGESTED -> Res.string.studio_notice_draft_exhausted
        StudioNotice.DRAFT_FAILED -> Res.string.studio_notice_draft_failed
        StudioNotice.QUOTE_REQUIRED -> Res.string.studio_notice_quote_required
        StudioNotice.RULE_ADDED -> Res.string.studio_notice_rule_added
        StudioNotice.PACK_MISSING -> Res.string.studio_notice_pack_missing
        StudioNotice.PACK_LOAD_FAILED -> Res.string.studio_notice_load_failed
        StudioNotice.AI_RULE_LOCKED -> Res.string.studio_notice_ai_rule_locked
        StudioNotice.EXPORT_UNAVAILABLE -> Res.string.studio_notice_export_unavailable
        StudioNotice.VALIDATION_REQUIRED -> Res.string.studio_notice_validation_required
        StudioNotice.DRAFT_EXPORTED -> Res.string.studio_notice_exported
        StudioNotice.SAVE_FAILED -> Res.string.studio_notice_save_failed
    },
)
