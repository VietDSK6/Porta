package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate

/**
 * Cycle state is kept separate from eligibility so an archived record is not mistaken for a live
 * opportunity whose published deadline merely passed.
 */
enum class OpportunityCycleStatus {
    ACTIVE,
    DEADLINE_TBA,
    DEADLINE_MISSING,
    CLOSED,
    ARCHIVED,
}

/** Porta's catalog-maintenance signal, not a provider endorsement or evidence verification. */
enum class SourceFreshness {
    CURRENT,
    REVIEW_DUE,
    PREDATES_CYCLE,
    FUTURE_DATED,
}

data class SourceFreshnessAssessment(
    val status: SourceFreshness,
    val daysSinceReview: Long,
)

fun Opportunity.cycleStatus(today: LocalDate): OpportunityCycleStatus = when {
    archived -> OpportunityCycleStatus.ARCHIVED
    deadline is Deadline.Known && deadline.date < today -> OpportunityCycleStatus.CLOSED
    deadline == Deadline.Tba -> OpportunityCycleStatus.DEADLINE_TBA
    deadline == Deadline.Missing -> OpportunityCycleStatus.DEADLINE_MISSING
    else -> OpportunityCycleStatus.ACTIVE
}

fun Opportunity.sourceFreshness(
    today: LocalDate,
    reviewWindowDays: Int = DEFAULT_SOURCE_REVIEW_WINDOW_DAYS,
): SourceFreshnessAssessment {
    require(reviewWindowDays > 0) { "Source review window must be positive." }
    val daysSinceReview = today.toEpochDays() - lastCheckedOn.toEpochDays()
    val status = when {
        daysSinceReview < 0 -> SourceFreshness.FUTURE_DATED
        cycleStartOn != null && lastCheckedOn < cycleStartOn -> SourceFreshness.PREDATES_CYCLE
        daysSinceReview > reviewWindowDays -> SourceFreshness.REVIEW_DUE
        else -> SourceFreshness.CURRENT
    }
    return SourceFreshnessAssessment(status, daysSinceReview)
}

const val DEFAULT_SOURCE_REVIEW_WINDOW_DAYS = 90
