package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EligibilityReceipt
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.RuleReceipt
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.ScenarioOverrides
import io.github.vietdsk6.porta.domain.model.findAtomicRule
import io.github.vietdsk6.porta.domain.model.fingerprint

/** Only templates explicitly linked to unmet sourced rules can become planning candidates. */
internal class CandidateCatalog {
    fun select(
        opportunities: List<Opportunity>,
        receipts: Map<String, EligibilityReceipt>,
    ): List<ActionTemplate> = opportunities.flatMap { opportunity ->
        val templates = opportunity.actionTemplates.associateBy(ActionTemplate::id)
        receipts.getValue(opportunity.id).rules.mapNotNull { receipt ->
            receipt.actionTemplateId
                ?.let(templates::get)
                ?.takeIf { template -> template.targetsReceipt(opportunity.id, receipt) }
                ?.takeIf { template -> template.canAddress(receipt.result) }
        }
    }
        .filter(ActionTemplate::isEthicallyActionable)
        .groupBy(ActionTemplate::target)
        .values
        .map { matching ->
            matching.minWith(
                compareBy<ActionTemplate>(ActionTemplate::minimumDurationDays)
                    .thenBy { it.effort.rank }
                    .thenBy { it.costBand.rank }
                    .thenBy(ActionTemplate::id),
            )
        }
        .sortedBy(ActionTemplate::id)
}

internal fun ActionTemplate.targetsReceipt(
    opportunityId: String,
    receipt: RuleReceipt,
): Boolean = when (val actionTarget = target) {
    is ActionTarget.ProfileFactTarget -> receipt.actionTemplateId == id && receipt.factKey == actionTarget.factKey

    is ActionTarget.OpportunityRuleTarget -> {
        receipt.actionTemplateId == id &&
            actionTarget.opportunityId == opportunityId &&
            actionTarget.ruleId == receipt.ruleId &&
            receipt.factKey == actionTarget.factKey
    }
}

internal fun ActionTemplate.canAddress(result: RuleResult): Boolean = when (target) {
    is ActionTarget.ProfileFactTarget -> result == RuleResult.FAIL
    is ActionTarget.OpportunityRuleTarget -> result == RuleResult.FAIL || result == RuleResult.UNKNOWN
}

internal fun ActionTemplate.scenarioOverrides(opportunityById: Map<String, Opportunity>): ScenarioOverrides = when (val actionTarget = target) {
    is ActionTarget.ProfileFactTarget -> ScenarioOverrides(
        profileFacts = mapOf(actionTarget.factKey to actionTarget.value),
    )

    is ActionTarget.OpportunityRuleTarget -> {
        val currentRule = opportunityById[actionTarget.opportunityId]
            ?.eligibilityRule
            ?.findAtomicRule(actionTarget.ruleId)
        if (
            currentRule == null ||
            currentRule.factKey != actionTarget.factKey ||
            currentRule.fingerprint() != actionTarget.ruleFingerprint
        ) {
            ScenarioOverrides()
        } else {
            ScenarioOverrides(
                ruleAssertions = mapOf(
                    RuleRef(actionTarget.opportunityId, actionTarget.ruleId) to actionTarget.value,
                ),
            )
        }
    }
}

internal fun ActionTemplate.isEthicallyActionable(): Boolean = targetActionability == Actionability.CONTROLLABLE || targetActionability == Actionability.TIME_DEPENDENT
