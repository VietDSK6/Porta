package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate

data class Opportunity(
    val id: String,
    val title: String,
    val provider: String,
    val category: OpportunityCategory,
    val cycle: String,
    val cycleStartOn: LocalDate? = null,
    val sourceUrl: String,
    val sourceSection: String? = null,
    val lastCheckedOn: LocalDate,
    val deadline: Deadline,
    val eligibilityRule: RuleNode,
    val softCriteria: List<String> = emptyList(),
    val readinessItems: List<String> = emptyList(),
    val actionTemplates: List<ActionTemplate> = emptyList(),
    val schemaVersion: Int = 1,
    val disclaimer: String = DEFAULT_DISCLAIMER,
    val archived: Boolean = false,
) {
    companion object {
        const val DEFAULT_DISCLAIMER =
            "Porta compares only the published rules encoded in this source pack. " +
                "Confirm every requirement and deadline with the official provider."
    }
}

enum class OpportunityCategory {
    SCHOLARSHIP,
    INTERNSHIP,
    ACADEMIC_PROGRAM,
    STUDENT_CONTEST,
    FELLOWSHIP,
}

sealed interface Deadline {
    data class Known(val date: LocalDate) : Deadline

    data object Tba : Deadline

    data object Missing : Deadline
}

sealed interface RuleNode {
    val id: String

    data class All(
        override val id: String,
        val children: List<RuleNode>,
    ) : RuleNode

    data class Any(
        override val id: String,
        val children: List<RuleNode>,
    ) : RuleNode

    data class Atomic(
        override val id: String,
        val factKey: FactKey,
        val operator: Operator,
        val expected: FactValue? = null,
        val explanation: String,
        val sourceNote: String? = null,
        val actionability: Actionability,
        val actionTemplateId: String? = null,
    ) : RuleNode

    data class ManualCheck(
        override val id: String,
        val explanation: String,
        val sourceNote: String? = null,
    ) : RuleNode
}

enum class Operator {
    EXISTS,
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN_OR_EQUAL,
    IN_SET,
    CONTAINS_ANY,
    CONTAINS_ALL,
    DATE_BEFORE,
    DATE_AFTER,
}

enum class RuleResult {
    PASS,
    FAIL,
    UNKNOWN,
    REVIEW,
}

enum class EligibilityState {
    OPEN,
    HAS_GAPS,
    NEEDS_INFO,
    NOT_ACTIONABLE,
    CLOSED,
}

data class RuleReceipt(
    val ruleId: String,
    val result: RuleResult,
    val explanation: String,
    val factKey: FactKey? = null,
    val actual: FactValue? = null,
    val expected: FactValue? = null,
    val sourceNote: String? = null,
    val actionability: Actionability? = null,
    val actionTemplateId: String? = null,
)

data class EligibilityReceipt(
    val opportunityId: String,
    val state: EligibilityState,
    val rootResult: RuleResult,
    val rules: List<RuleReceipt>,
    val deadline: Deadline,
    val lastCheckedOn: LocalDate,
    val cycleStatus: OpportunityCycleStatus = OpportunityCycleStatus.ACTIVE,
)
