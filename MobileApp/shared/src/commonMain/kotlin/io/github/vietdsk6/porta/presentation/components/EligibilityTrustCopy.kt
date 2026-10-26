package io.github.vietdsk6.porta.presentation.components

import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityReceipt
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.OpportunityCycleStatus
import io.github.vietdsk6.porta.domain.model.RuleResult

/**
 * User-facing language for Porta's deterministic rule assessment.
 *
 * These labels deliberately avoid implying that Porta verified evidence, established full
 * eligibility, or predicted selection. Internal eligibility enums remain stable for persistence.
 */
fun EligibilityState.trustLabel(): String = when (this) {
    EligibilityState.OPEN -> "Assessed rules passed"
    EligibilityState.HAS_GAPS -> "Needs action"
    EligibilityState.NEEDS_INFO -> "Needs information"
    EligibilityState.NOT_ACTIONABLE -> "Not available this cycle"
    EligibilityState.CLOSED -> "Closed"
}

fun EligibilityReceipt.headlineLabel(): String = when {
    cycleStatus == OpportunityCycleStatus.ARCHIVED -> "Archived cycle"
    state == EligibilityState.CLOSED -> state.trustLabel()
    rootResult == RuleResult.PASS && deadline !is Deadline.Known -> "Timing needs confirmation"
    else -> state.trustLabel()
}

fun EligibilityReceipt.requirementAssessmentLabel(): String = if (cycleStatus == OpportunityCycleStatus.ARCHIVED) {
    "Archived cycle"
} else {
    when (rootResult) {
        RuleResult.PASS -> "Assessed rules passed"

        RuleResult.FAIL -> when (state) {
            EligibilityState.HAS_GAPS -> "Needs action"
            EligibilityState.NOT_ACTIONABLE -> "Not available this cycle"
            else -> state.trustLabel()
        }

        RuleResult.UNKNOWN -> "Needs information"

        RuleResult.REVIEW -> "Needs source review"
    }
}

fun EligibilityReceipt.timingLabel(): String = when {
    cycleStatus == OpportunityCycleStatus.ARCHIVED -> "Timing: archived cycle retained for reference"
    state == EligibilityState.CLOSED -> "Timing: application window closed"
    deadline is Deadline.Known -> "Timing: published deadline ${deadline.date}"
    deadline == Deadline.Tba -> "Timing: provider date not yet published"
    else -> "Timing: deadline not found in this source record"
}

fun EligibilityReceipt.trustSummary(): String = when {
    cycleStatus == OpportunityCycleStatus.ARCHIVED -> {
        "This record belongs to an archived cycle. Porta keeps it for reference but does not treat it as currently available."
    }

    state == EligibilityState.CLOSED -> "The published application window is closed for this cycle."

    rootResult == RuleResult.PASS && deadline !is Deadline.Known -> {
        "The assessed rules pass, but Porta cannot confirm the current application timing from this source record."
    }

    state == EligibilityState.OPEN -> {
        "The published rules Porta can assess currently pass. This does not establish full eligibility or predict selection."
    }

    state == EligibilityState.HAS_GAPS -> "At least one assessed rule needs a change in your profile."

    state == EligibilityState.NEEDS_INFO -> "A fact is missing or a published requirement still needs source review."

    state == EligibilityState.NOT_ACTIONABLE -> {
        "A current-cycle requirement cannot be addressed through a profile update."
    }

    else -> "Porta cannot form a current conclusion from the available information."
}
