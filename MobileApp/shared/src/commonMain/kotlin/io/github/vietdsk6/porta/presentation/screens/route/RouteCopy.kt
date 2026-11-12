package io.github.vietdsk6.porta.presentation.screens.route

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.generated.resources.*
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun routeCostLabel(cost: CostBand): String = stringResource(
    when (cost) {
        CostBand.FREE -> Res.string.route_cost_free
        CostBand.LOW -> Res.string.route_cost_low
        CostBand.MEDIUM -> Res.string.route_cost_medium
        CostBand.HIGH -> Res.string.route_cost_high
        CostBand.UNKNOWN -> Res.string.route_cost_any
    },
)

@Composable
internal fun routeEffortLabel(effort: EffortLevel): String = stringResource(
    when (effort) {
        EffortLevel.VERY_LOW -> Res.string.route_effort_very_low
        EffortLevel.LOW -> Res.string.route_effort_low
        EffortLevel.MEDIUM -> Res.string.route_effort_medium
        EffortLevel.HIGH -> Res.string.route_effort_high
        EffortLevel.VERY_HIGH -> Res.string.route_effort_very_high
    },
)

@Composable
internal fun routeDuration(days: Int): String = pluralStringResource(Res.plurals.route_duration_days, days, days)

@Composable
internal fun routeSummary(route: RouteOptionUiState): String {
    val outcome = if (route.openedTotal > 0) {
        pluralStringResource(Res.plurals.route_summary_open, route.openedTotal, route.openedTotal)
    } else {
        pluralStringResource(Res.plurals.route_summary_improved, route.improvedDoorCount, route.improvedDoorCount)
    }
    val saved = if (route.openedSaved > 0) {
        pluralStringResource(Res.plurals.route_summary_saved, route.openedSaved, route.openedSaved)
    } else {
        ""
    }
    val span = stringResource(Res.string.route_summary_span, route.steps.size, route.totalDays)
    val lost = if (route.lostDoorCount > 0) {
        pluralStringResource(Res.plurals.route_summary_lost, route.lostDoorCount, route.lostDoorCount)
    } else {
        ""
    }
    val lostSentence = if (lost.isEmpty()) "" else " $lost"
    return "$outcome$saved $span$lostSentence ${stringResource(Res.string.route_summary_verify)}"
}

@Composable
internal fun routeReason(reason: RouteReason): String = when (reason.kind) {
    RouteReasonKind.OPENS_SAVED -> pluralStringResource(Res.plurals.route_reason_saved, reason.count, reason.count)
    RouteReasonKind.OPENS -> pluralStringResource(Res.plurals.route_reason_open, reason.count, reason.count)
    RouteReasonKind.IMPROVES -> pluralStringResource(Res.plurals.route_reason_improve, reason.count, reason.count)
}

@Composable
internal fun routeNotice(notice: RouteNotice): String = stringResource(
    when (notice) {
        RouteNotice.SAVED -> Res.string.route_notice_saved
        RouteNotice.SAVE_FAILED -> Res.string.route_notice_error
    },
)
