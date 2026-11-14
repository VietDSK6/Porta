package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.SourceFreshness
import io.github.vietdsk6.porta.presentation.components.EligibilityCopyCode
import kotlinx.datetime.LocalDate

data class OpportunityDetailUiState(
    val id: String = "",
    val title: String = "",
    val provider: String = "",
    val state: EligibilityState? = null,
    val rootResult: RuleResult? = null,
    val copyCode: EligibilityCopyCode? = null,
    val stateLabel: String = "",
    val conclusionSummary: String = "",
    val timingLabel: String = "",
    val deadlineLabel: String = "",
    val deadlineDate: LocalDate? = null,
    val deadlineKind: DeadlineKind? = null,
    val checkedLabel: String = "",
    val sourceFreshness: SourceFreshness? = null,
    val sourceCheckedOn: LocalDate? = null,
    val sourceReviewNeedsAttention: Boolean = false,
    val isArchived: Boolean = false,
    val sourceUrl: String = "",
    val sourceSection: String = "",
    val disclaimer: String = "",
    val rules: List<RuleReceiptUiState> = emptyList(),
    val publishedPath: PublishedCheckPath? = null,
    val softCriteria: List<String> = emptyList(),
    val relatedActionId: String? = null,
    val relatedActionTitle: String = "",
    val nextStepGuidance: String = "",
    val nextStepCode: NextStepCode? = null,
    val informationTasks: List<InformationTaskUiState> = emptyList(),
    val canOpenSource: Boolean = false,
    val isSampleProfile: Boolean = true,
    val isDemoProfile: Boolean = false,
    val isSampleCatalog: Boolean = true,
    val error: String? = null,
    val errorCode: DetailErrorCode? = null,
)

enum class DeadlineKind { KNOWN, MISSING, TBA }

enum class NextStepCode { LINKED_ACTION, MISSING_FACTS, TIMING_MISMATCH, VERIFY_SOURCE, ASSESSED_PATH_REVIEW, NO_CHANGE }

enum class DetailErrorCode { OPPORTUNITY_MISSING }

data class RuleReceiptUiState(
    val id: String,
    val result: RuleResult,
    val explanation: String,
    val actualLabel: String,
    val expectedLabel: String,
    val sourceNote: String,
    val shortTitle: String = "",
    val titleFactKey: FactKey? = null,
    val actualMissing: Boolean = false,
    val expectedManualReview: Boolean = false,
)

data class InformationTaskUiState(
    val id: String,
    val factKey: FactKey,
    val title: String,
    val question: String,
    val sourceNote: String,
    val destination: InformationTaskDestination,
    val titleFactKey: FactKey? = null,
)

enum class InformationTaskDestination {
    ACADEMIC_PROFILE,
    OFFICIAL_SOURCE,
}

sealed interface OpportunityDetailUiEvent {
    data object Acknowledged : OpportunityDetailUiEvent
}
