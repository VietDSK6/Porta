package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate

/** Limits a route's elapsed time, estimated cost and effort per milestone, and number of steps. */
data class RouteConstraints(
    val horizonDays: Int,
    val maxCostPerStep: CostBand,
    val maxEffort: EffortLevel,
    val maxSteps: Int = 4,
    val prioritizeSaved: Boolean = true,
)

/**
 * The checks that change by the end of one milestone; no result predicts selection by a provider.
 *
 * [doorsOpened], [doorsReadyForReview] and [doorsImproved] are disjoint. A door is ready for review when every
 * encoded check passes and only manual checks or an unconfirmed deadline remain
 * (see [EligibilityReceipt.readyForOfficialReview]).
 */
data class RouteStep(
    val template: ActionTemplate,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val doorsOpened: List<String>,
    val doorsImproved: List<String>,
    val doorsLost: List<String>,
    val doorsReadyForReview: List<String> = emptyList(),
)

/**
 * Doors count when they first open, or first become ready for official review, before their deadline. Later
 * expiry remains visible in [RouteStep.doorsLost].
 */
data class RouteMetrics(
    val openedTotal: Int,
    val openedSaved: Int,
    val daysToFirstDoor: Int?,
    val totalDays: Int,
    val costRankSum: Int,
    val peakEffort: EffortLevel,
    val readyForReviewTotal: Int = 0,
    val readyForReviewSaved: Int = 0,
)

/** Localizable reason for the route's place in the comparison. */
sealed interface RouteExplanation {
    data class OpensSavedDoors(val count: Int) : RouteExplanation

    data class OpensDoors(val count: Int) : RouteExplanation

    data class ReadiesDoorsForReview(val count: Int) : RouteExplanation

    data class ImprovesDoors(val count: Int) : RouteExplanation
}

data class PlannedRoute(
    /** Stable for the same ordered milestones, suitable for grouping saved plan items. */
    val id: String,
    val steps: List<RouteStep>,
    val metrics: RouteMetrics,
    val explanation: RouteExplanation,
)
