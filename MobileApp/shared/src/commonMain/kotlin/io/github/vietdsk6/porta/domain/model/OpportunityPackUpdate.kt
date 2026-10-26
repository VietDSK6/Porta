package io.github.vietdsk6.porta.domain.model

data class OpportunityPackImportPreview(
    val document: OpportunityPackDocument,
    val diff: OpportunityPackDiff,
)

data class OpportunityPackDiff(
    val previousPackId: String?,
    val previousVersion: Int?,
    val candidatePackId: String,
    val candidateVersion: Int,
    val addedOpportunityIds: Set<String>,
    val removedOpportunityIds: Set<String>,
    val changedOpportunityIds: Set<String>,
    val unchangedOpportunityIds: Set<String>,
) {
    val hasChanges: Boolean
        get() = addedOpportunityIds.isNotEmpty() ||
            removedOpportunityIds.isNotEmpty() ||
            changedOpportunityIds.isNotEmpty()
}

fun OpportunityPackDocument.diffTo(candidate: OpportunityPackDocument): OpportunityPackDiff {
    val previousById = opportunities.associateBy(Opportunity::id)
    val candidateById = candidate.opportunities.associateBy(Opportunity::id)
    val previousIds = previousById.keys
    val candidateIds = candidateById.keys
    val sharedIds = previousIds intersect candidateIds
    val changedIds = sharedIds.filterTo(mutableSetOf()) { opportunityId ->
        previousById.getValue(opportunityId) != candidateById.getValue(opportunityId)
    }
    return OpportunityPackDiff(
        previousPackId = metadata.id,
        previousVersion = metadata.version,
        candidatePackId = candidate.metadata.id,
        candidateVersion = candidate.metadata.version,
        addedOpportunityIds = candidateIds - previousIds,
        removedOpportunityIds = previousIds - candidateIds,
        changedOpportunityIds = changedIds,
        unchangedOpportunityIds = sharedIds - changedIds,
    )
}

fun OpportunityPackDocument.diffFromEmpty(): OpportunityPackDiff = OpportunityPackDiff(
    previousPackId = null,
    previousVersion = null,
    candidatePackId = metadata.id,
    candidateVersion = metadata.version,
    addedOpportunityIds = opportunities.mapTo(mutableSetOf(), Opportunity::id),
    removedOpportunityIds = emptySet(),
    changedOpportunityIds = emptySet(),
    unchangedOpportunityIds = emptySet(),
)

/**
 * Returns stored programme confirmations that cannot be applied to [candidate]. They remain in
 * local history; the eligibility evaluator ignores them because the exact rule fingerprint no longer
 * matches.
 */
fun ProfileSnapshot.staleRuleAssertionsFor(
    candidate: OpportunityPackDocument,
): List<RuleAssertion> = ruleAssertions.values.filter { assertion ->
    val opportunity = candidate.opportunities.firstOrNull { opportunity ->
        opportunity.id == assertion.ruleRef.opportunityId
    }
    val candidateFingerprint = opportunity?.eligibilityRule
        ?.findAtomicRule(assertion.ruleRef.ruleId)
        ?.fingerprint()
    candidateFingerprint != assertion.ruleFingerprint
}
