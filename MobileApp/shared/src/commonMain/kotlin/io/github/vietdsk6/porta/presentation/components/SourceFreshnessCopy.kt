package io.github.vietdsk6.porta.presentation.components

import io.github.vietdsk6.porta.domain.model.DEFAULT_SOURCE_REVIEW_WINDOW_DAYS
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.SourceFreshness
import io.github.vietdsk6.porta.domain.model.sourceFreshness
import kotlinx.datetime.LocalDate

fun Opportunity.sourceReviewLabel(today: LocalDate): String = when (sourceFreshness(today).status) {
    SourceFreshness.CURRENT -> {
        "Source reviewed on $lastCheckedOn · within Porta's $DEFAULT_SOURCE_REVIEW_WINDOW_DAYS-day review window"
    }

    SourceFreshness.REVIEW_DUE -> "Porta source review due · last checked on $lastCheckedOn"

    SourceFreshness.PREDATES_CYCLE -> "Source review predates the listed cycle · last checked on $lastCheckedOn"

    SourceFreshness.FUTURE_DATED -> "Source review date needs correction · recorded as $lastCheckedOn"
}

fun Opportunity.sourceReviewNeedsAttention(today: LocalDate): Boolean = sourceFreshness(today).status != SourceFreshness.CURRENT
