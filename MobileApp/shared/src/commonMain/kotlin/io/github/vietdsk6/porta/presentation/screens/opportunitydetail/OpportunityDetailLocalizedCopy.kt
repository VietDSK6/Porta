package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.DEFAULT_SOURCE_REVIEW_WINDOW_DAYS
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.SourceFreshness
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.presentation.locale.formatLocalDate
import io.github.vietdsk6.porta.presentation.locale.toText
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OpportunityDetailUiState.localizedError(): String = when (errorCode) {
    DetailErrorCode.OPPORTUNITY_MISSING -> stringResource(Res.string.detail_error_missing)
    null -> error.orEmpty()
}

@Composable
internal fun OpportunityDetailUiState.localizedDeadline(): String = when (deadlineKind) {
    DeadlineKind.KNOWN -> stringResource(Res.string.detail_deadline_known, deadlineDate?.let(::formatLocalDate).orEmpty())
    DeadlineKind.MISSING -> stringResource(Res.string.detail_deadline_missing)
    DeadlineKind.TBA -> stringResource(Res.string.detail_deadline_tba)
    null -> deadlineLabel
}

@Composable
internal fun OpportunityDetailUiState.localizedSourceReview(): String {
    val date = sourceCheckedOn?.let(::formatLocalDate).orEmpty()
    return when (sourceFreshness) {
        SourceFreshness.CURRENT -> stringResource(Res.string.detail_source_current, date, DEFAULT_SOURCE_REVIEW_WINDOW_DAYS)
        SourceFreshness.REVIEW_DUE -> stringResource(Res.string.detail_source_due, date)
        SourceFreshness.PREDATES_CYCLE -> stringResource(Res.string.detail_source_predates, date)
        SourceFreshness.FUTURE_DATED -> stringResource(Res.string.detail_source_future, date)
        null -> checkedLabel
    }
}

@Composable
internal fun OpportunityDetailUiState.localizedNextStep(): String = when (nextStepCode) {
    NextStepCode.LINKED_ACTION -> stringResource(Res.string.detail_guidance_linked)
    NextStepCode.MISSING_FACTS -> stringResource(Res.string.detail_guidance_missing)
    NextStepCode.TIMING_MISMATCH -> stringResource(Res.string.detail_guidance_timing)
    NextStepCode.VERIFY_SOURCE -> stringResource(Res.string.detail_guidance_verify)
    NextStepCode.ASSESSED_PATH_REVIEW -> stringResource(Res.string.detail_guidance_assessed_path_review)
    NextStepCode.NO_CHANGE -> stringResource(Res.string.detail_guidance_none)
    null -> nextStepGuidance
}

@Composable
internal fun InformationTaskUiState.localizedTitle(): String = when (titleFactKey) {
    FactKey.DEGREE_COMPLETED -> stringResource(Res.string.detail_task_degree)
    FactKey.ENROLLED -> stringResource(Res.string.detail_task_enrolled)
    FactKey.EXPERIENCE_MONTHS -> stringResource(Res.string.detail_task_experience)
    FactKey.DATE_OF_BIRTH -> stringResource(Res.string.detail_task_birth)
    null -> title
    else -> stringResource(Res.string.detail_task_other, titleFactKey.toText())
}

@Composable
internal fun RuleReceiptUiState.localizedShortTitle(): String = when {
    titleFactKey != null -> stringResource(Res.string.detail_fact_check, titleFactKey.toText())
    shortTitle.isBlank() || shortTitle == "Programme condition" -> stringResource(Res.string.detail_programme_condition)
    else -> shortTitle
}

@Composable
internal fun RuleReceiptUiState.localizedActual(): String = if (actualMissing) {
    stringResource(Res.string.detail_not_provided)
} else {
    actualLabel
}

@Composable
internal fun RuleReceiptUiState.localizedExpected(): String = if (expectedManualReview) {
    stringResource(Res.string.detail_manual_review)
} else {
    expectedLabel
}
