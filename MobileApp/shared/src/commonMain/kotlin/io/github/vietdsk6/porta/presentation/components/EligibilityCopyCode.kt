package io.github.vietdsk6.porta.presentation.components

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityReceipt
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.OpportunityCycleStatus
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.presentation.locale.formatLocalDate
import io.github.vietdsk6.porta.presentation.locale.toText
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Locale-neutral presentation choices derived from the deterministic receipt. */
data class EligibilityCopyCode(
    val headline: Headline,
    val requirement: Headline,
    val reason: Reason,
    val reasonFactKey: FactKey?,
    val timing: Timing,
    val deadlineDate: LocalDate?,
    val summary: Summary,
) {
    enum class Headline { PASSED, NEEDS_ACTION, NEEDS_INFO, NOT_AVAILABLE, CLOSED, ARCHIVED, TIMING, SOURCE_REVIEW }
    enum class Reason {
        PAST_CYCLE,
        CLOSED,
        NOT_ACTIONABLE,
        PASS_TIMING,
        PASS,
        FAIL_FACT,
        FAIL_GENERIC,
        MANUAL,
        MISSING_FACT,
        MISSING_GENERIC,
        DEFAULT,
    }
    enum class Timing { ARCHIVED, CLOSED, KNOWN, TBA, MISSING }
    enum class Summary { ARCHIVED, CLOSED, PASS_TIMING, OPEN, GAPS, INFO, NOT_ACTIONABLE, DEFAULT }
}

fun EligibilityReceipt.displayCode(): EligibilityCopyCode {
    val archived = cycleStatus == OpportunityCycleStatus.ARCHIVED
    val firstFailure = rules.firstOrNull { it.result == RuleResult.FAIL }
    val firstUnknown = rules.firstOrNull { it.result == RuleResult.UNKNOWN }
    val factKey = when {
        rootResult == RuleResult.FAIL -> firstFailure?.factKey
        rootResult == RuleResult.UNKNOWN -> firstUnknown?.factKey
        else -> null
    }
    val reason = when {
        archived -> EligibilityCopyCode.Reason.PAST_CYCLE
        state == EligibilityState.CLOSED -> EligibilityCopyCode.Reason.CLOSED
        state == EligibilityState.NOT_ACTIONABLE -> EligibilityCopyCode.Reason.NOT_ACTIONABLE
        rootResult == RuleResult.PASS && deadline !is Deadline.Known -> EligibilityCopyCode.Reason.PASS_TIMING
        rootResult == RuleResult.PASS -> EligibilityCopyCode.Reason.PASS
        rootResult == RuleResult.FAIL && factKey != null -> EligibilityCopyCode.Reason.FAIL_FACT
        rootResult == RuleResult.FAIL -> EligibilityCopyCode.Reason.FAIL_GENERIC
        rootResult == RuleResult.REVIEW -> EligibilityCopyCode.Reason.MANUAL
        firstUnknown?.factKey != null -> EligibilityCopyCode.Reason.MISSING_FACT
        firstUnknown != null -> EligibilityCopyCode.Reason.MISSING_GENERIC
        rules.any { it.result == RuleResult.REVIEW } -> EligibilityCopyCode.Reason.MANUAL
        else -> EligibilityCopyCode.Reason.DEFAULT
    }
    val headline = when {
        archived -> EligibilityCopyCode.Headline.ARCHIVED
        state == EligibilityState.CLOSED -> EligibilityCopyCode.Headline.CLOSED
        rootResult == RuleResult.PASS && deadline !is Deadline.Known -> EligibilityCopyCode.Headline.TIMING
        else -> state.headlineCode()
    }
    val requirement = when {
        archived -> EligibilityCopyCode.Headline.ARCHIVED
        rootResult == RuleResult.PASS -> EligibilityCopyCode.Headline.PASSED
        rootResult == RuleResult.UNKNOWN -> EligibilityCopyCode.Headline.NEEDS_INFO
        rootResult == RuleResult.REVIEW -> EligibilityCopyCode.Headline.SOURCE_REVIEW
        state == EligibilityState.HAS_GAPS -> EligibilityCopyCode.Headline.NEEDS_ACTION
        state == EligibilityState.NOT_ACTIONABLE -> EligibilityCopyCode.Headline.NOT_AVAILABLE
        else -> state.headlineCode()
    }
    val timing = when {
        archived -> EligibilityCopyCode.Timing.ARCHIVED
        state == EligibilityState.CLOSED -> EligibilityCopyCode.Timing.CLOSED
        deadline is Deadline.Known -> EligibilityCopyCode.Timing.KNOWN
        deadline == Deadline.Tba -> EligibilityCopyCode.Timing.TBA
        else -> EligibilityCopyCode.Timing.MISSING
    }
    val summary = when {
        archived -> EligibilityCopyCode.Summary.ARCHIVED
        state == EligibilityState.CLOSED -> EligibilityCopyCode.Summary.CLOSED
        rootResult == RuleResult.PASS && deadline !is Deadline.Known -> EligibilityCopyCode.Summary.PASS_TIMING
        state == EligibilityState.OPEN -> EligibilityCopyCode.Summary.OPEN
        state == EligibilityState.HAS_GAPS -> EligibilityCopyCode.Summary.GAPS
        state == EligibilityState.NEEDS_INFO -> EligibilityCopyCode.Summary.INFO
        state == EligibilityState.NOT_ACTIONABLE -> EligibilityCopyCode.Summary.NOT_ACTIONABLE
        else -> EligibilityCopyCode.Summary.DEFAULT
    }
    return EligibilityCopyCode(
        headline = headline,
        requirement = requirement,
        reason = reason,
        reasonFactKey = factKey,
        timing = timing,
        deadlineDate = (deadline as? Deadline.Known)?.date,
        summary = summary,
    )
}

private fun EligibilityState.headlineCode(): EligibilityCopyCode.Headline = when (this) {
    EligibilityState.OPEN -> EligibilityCopyCode.Headline.PASSED
    EligibilityState.HAS_GAPS -> EligibilityCopyCode.Headline.NEEDS_ACTION
    EligibilityState.NEEDS_INFO -> EligibilityCopyCode.Headline.NEEDS_INFO
    EligibilityState.NOT_ACTIONABLE -> EligibilityCopyCode.Headline.NOT_AVAILABLE
    EligibilityState.CLOSED -> EligibilityCopyCode.Headline.CLOSED
}

@Composable
fun EligibilityState.trustLabelText(): String = stringResource(headlineCode().resource())

@Composable
fun EligibilityCopyCode.headlineText(): String = stringResource(headline.resource())

@Composable
fun EligibilityCopyCode.requirementText(): String = stringResource(requirement.resource())

@Composable
fun EligibilityCopyCode.reasonText(): String = when (reason) {
    EligibilityCopyCode.Reason.FAIL_FACT -> stringResource(
        Res.string.eligibility_reason_fail_fact,
        reasonFactKey?.toText().orEmpty(),
    )

    EligibilityCopyCode.Reason.MISSING_FACT -> stringResource(
        Res.string.eligibility_reason_missing_fact,
        reasonFactKey?.toText().orEmpty(),
    )

    else -> stringResource(reason.resource())
}

@Composable
fun EligibilityCopyCode.timingText(): String = if (timing == EligibilityCopyCode.Timing.KNOWN) {
    stringResource(Res.string.eligibility_timing_known, deadlineDate?.let(::formatLocalDate).orEmpty())
} else {
    stringResource(timing.resource())
}

@Composable
fun EligibilityCopyCode.summaryText(): String = stringResource(summary.resource())

private fun EligibilityCopyCode.Headline.resource(): StringResource = when (this) {
    EligibilityCopyCode.Headline.PASSED -> Res.string.eligibility_status_passed
    EligibilityCopyCode.Headline.NEEDS_ACTION -> Res.string.eligibility_status_needs_action
    EligibilityCopyCode.Headline.NEEDS_INFO -> Res.string.eligibility_status_needs_info
    EligibilityCopyCode.Headline.NOT_AVAILABLE -> Res.string.eligibility_status_not_available
    EligibilityCopyCode.Headline.CLOSED -> Res.string.eligibility_status_closed
    EligibilityCopyCode.Headline.ARCHIVED -> Res.string.eligibility_status_archived
    EligibilityCopyCode.Headline.TIMING -> Res.string.eligibility_status_timing
    EligibilityCopyCode.Headline.SOURCE_REVIEW -> Res.string.eligibility_requirement_source_review
}

private fun EligibilityCopyCode.Reason.resource(): StringResource = when (this) {
    EligibilityCopyCode.Reason.PAST_CYCLE -> Res.string.eligibility_reason_past_cycle
    EligibilityCopyCode.Reason.CLOSED -> Res.string.eligibility_reason_closed
    EligibilityCopyCode.Reason.NOT_ACTIONABLE -> Res.string.eligibility_reason_not_actionable
    EligibilityCopyCode.Reason.PASS_TIMING -> Res.string.eligibility_reason_pass_timing
    EligibilityCopyCode.Reason.PASS -> Res.string.eligibility_reason_pass
    EligibilityCopyCode.Reason.FAIL_FACT -> Res.string.eligibility_reason_fail_fact
    EligibilityCopyCode.Reason.FAIL_GENERIC -> Res.string.eligibility_reason_fail_generic
    EligibilityCopyCode.Reason.MANUAL -> Res.string.eligibility_reason_manual
    EligibilityCopyCode.Reason.MISSING_FACT -> Res.string.eligibility_reason_missing_fact
    EligibilityCopyCode.Reason.MISSING_GENERIC -> Res.string.eligibility_reason_missing_generic
    EligibilityCopyCode.Reason.DEFAULT -> Res.string.eligibility_reason_default
}

private fun EligibilityCopyCode.Timing.resource(): StringResource = when (this) {
    EligibilityCopyCode.Timing.ARCHIVED -> Res.string.eligibility_timing_archived
    EligibilityCopyCode.Timing.CLOSED -> Res.string.eligibility_timing_closed
    EligibilityCopyCode.Timing.KNOWN -> Res.string.eligibility_timing_known
    EligibilityCopyCode.Timing.TBA -> Res.string.eligibility_timing_tba
    EligibilityCopyCode.Timing.MISSING -> Res.string.eligibility_timing_missing
}

private fun EligibilityCopyCode.Summary.resource(): StringResource = when (this) {
    EligibilityCopyCode.Summary.ARCHIVED -> Res.string.eligibility_summary_archived
    EligibilityCopyCode.Summary.CLOSED -> Res.string.eligibility_summary_closed
    EligibilityCopyCode.Summary.PASS_TIMING -> Res.string.eligibility_summary_pass_timing
    EligibilityCopyCode.Summary.OPEN -> Res.string.eligibility_summary_open
    EligibilityCopyCode.Summary.GAPS -> Res.string.eligibility_summary_gaps
    EligibilityCopyCode.Summary.INFO -> Res.string.eligibility_summary_info
    EligibilityCopyCode.Summary.NOT_ACTIONABLE -> Res.string.eligibility_summary_not_actionable
    EligibilityCopyCode.Summary.DEFAULT -> Res.string.eligibility_summary_default
}
