package io.github.vietdsk6.porta.domain.model

/**
 * Editable, non-persistent representation of one opportunity being authored in Opportunity Studio.
 *
 * Studio deliberately limits the first authoring surface to a flat list below one `ALL` or `ANY`
 * root. The exported pack still uses the full pack schema and the normal validator boundary.
 */
data class OpportunityStudioDraft(
    val opportunityId: String = "",
    val title: String = "",
    val provider: String = "",
    val category: OpportunityCategory = OpportunityCategory.SCHOLARSHIP,
    val cycle: String = "",
    val cycleStartOn: String = "",
    val sourceUrl: String = "",
    val sourceSection: String = "",
    val lastCheckedOn: String = "",
    val deadlineKind: StudioDeadlineKind = StudioDeadlineKind.TBA,
    val deadlineDate: String = "",
    val rootRuleKind: StudioRootRuleKind = StudioRootRuleKind.ALL,
    val rules: List<OpportunityStudioRuleDraft> = emptyList(),
)

enum class StudioDeadlineKind {
    KNOWN,
    TBA,
    MISSING,
}

enum class StudioRootRuleKind {
    ALL,
    ANY,
}

sealed interface OpportunityStudioRuleDraft {
    val id: String

    data class Atomic(
        override val id: String = "",
        val factKey: FactKey = FactKey.GPA,
        val operator: Operator = Operator.EXISTS,
        val expected: OpportunityStudioExpectedValueDraft? = null,
        val explanation: String = "",
        val sourceNote: String = "",
        val actionability: Actionability = Actionability.UNKNOWN,
    ) : OpportunityStudioRuleDraft

    data class ManualCheck(
        override val id: String = "",
        val explanation: String = "",
        val sourceNote: String = "",
    ) : OpportunityStudioRuleDraft
}

data class OpportunityStudioExpectedValueDraft(
    val kind: StudioFactValueKind,
    val value: String = "",
    val unit: String = "",
)

enum class StudioFactValueKind {
    BOOLEAN,
    NUMBER,
    ENUM,
    ENUM_LIST,
    TEXT,
    DATE,
    DURATION,
}
