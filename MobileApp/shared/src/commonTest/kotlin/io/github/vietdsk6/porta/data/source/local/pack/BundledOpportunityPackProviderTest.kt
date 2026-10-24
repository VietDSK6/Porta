package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BundledOpportunityPackProviderTest {
    @Test
    fun `bundled catalog is a valid sourced fifteen-record pack`() {
        val codec = OpportunityPackCodec()
        val result = OpportunityPackParser(codec, OpportunityPackValidator()).parse(
            rawJson = BundledOpportunityPackProvider(codec).rawJson(),
            importedAtEpochMillis = 42L,
        )

        assertTrue(result.isValid, result.diagnostics.joinToString())
        val document = assertNotNull(result.document)
        val opportunities = document.opportunities
        assertEquals(BundledOpportunityPackProvider.PACK_ID, document.metadata.id)
        assertEquals(15, opportunities.size)
        assertTrue(opportunities.all { it.eligibilityRule.id.isNotBlank() })
        assertTrue(opportunities.all { it.sourceUrl.startsWith("https://") })
        assertTrue(opportunities.all { it.lastCheckedOn.toString() == PortaContestPackData.REVIEWED_ON })
        assertFalse(opportunities.any { "example.invalid" in it.sourceUrl })
        assertTrue(opportunities.count { it.softCriteria.isNotEmpty() } >= 3)
        assertTrue(opportunities.sumOf { it.actionTemplates.size } >= 5)
        assertTrue(opportunities.flatMap { it.eligibilityRule.flatten() }.count { it is RuleNode.ManualCheck } >= 3)
        assertTrue(
            opportunities
                .flatMap { it.eligibilityRule.flatten() }
                .filterIsInstance<RuleNode.Atomic>()
                .count { it.actionability == Actionability.IMMUTABLE } >= 3,
        )

        val sharedProfileActionSignatures = opportunities
            .flatMap { it.actionTemplates }
            .filter { it.target is ActionTarget.ProfileFactTarget }
            .groupBy { "${it.targetFactKey}:${it.targetValue}" }
            .count { (_, actions) -> actions.size > 1 }
        assertTrue(sharedProfileActionSignatures >= 2)

        val availabilityTargets = opportunities
            .flatMap { it.actionTemplates }
            .mapNotNull { it.target as? ActionTarget.OpportunityRuleTarget }
            .filter { it.factKey == FactKey.AVAILABLE_FULL_PERIOD }
        val availabilityActions = opportunities
            .flatMap { it.actionTemplates }
            .filter { it.targetFactKey == FactKey.AVAILABLE_FULL_PERIOD }
        assertEquals(4, availabilityTargets.size)
        assertEquals(4, availabilityTargets.map { it.opportunityId to it.ruleId }.distinct().size)
        assertTrue(availabilityTargets.all { it.ruleFingerprint.isNotBlank() })
        assertEquals(4, availabilityActions.map { it.title }.distinct().size)
        assertTrue(availabilityActions.all { it.title.startsWith("Confirm full-period availability for ") })
        assertTrue(availabilityActions.all { "14-day confirmation lead time is a Porta assumption" in it.sourceNote.orEmpty() })
    }

    @Test
    fun `every bundled record yields sourced receipts for contrasting profiles`() {
        val codec = OpportunityPackCodec()
        val document = assertNotNull(
            OpportunityPackParser(codec, OpportunityPackValidator())
                .parse(BundledOpportunityPackProvider(codec).rawJson())
                .document,
        )
        val profiles = listOf(
            profile(
                FactKey.AGE to FactValue.NumberValue(20.0, "years"),
                FactKey.ENROLLED to FactValue.BooleanValue(true),
                FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(false),
                FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(false),
                FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(6.0, "months"),
            ),
            profile(
                FactKey.AGE to FactValue.NumberValue(30.0, "years"),
                FactKey.ENROLLED to FactValue.BooleanValue(false),
                FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(true),
                FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(true),
                FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(36.0, "months"),
            ),
        )
        val states = profiles.flatMap { profile ->
            document.opportunities.map { opportunity ->
                EligibilityEvaluator()
                    .evaluate(profile, opportunity, LocalDate(2026, 10, 21))
                    .also { receipt ->
                        assertTrue(receipt.rules.isNotEmpty(), opportunity.id)
                        assertTrue(receipt.rules.all { !it.sourceNote.isNullOrBlank() }, opportunity.id)
                    }
                    .state
            }
        }.toSet()

        assertTrue(EligibilityState.NEEDS_INFO in states)
        assertTrue(EligibilityState.HAS_GAPS in states)
        assertTrue(EligibilityState.NOT_ACTIONABLE in states)
        assertTrue(EligibilityState.CLOSED in states)
    }

    @Test
    fun `bundled catalog produces a multi-door frontier from non-language facts`() {
        val codec = OpportunityPackCodec()
        val opportunities = assertNotNull(
            OpportunityPackParser(codec, OpportunityPackValidator())
                .parse(BundledOpportunityPackProvider(codec).rawJson())
                .document,
        ).opportunities
        val eligibilityEvaluator = EligibilityEvaluator()
        val frontier = ActionFrontierEngine(
            eligibilityEvaluator = eligibilityEvaluator,
            scenarioEvaluator = ScenarioEvaluator(eligibilityEvaluator),
        ).build(
            profile = profile(
                FactKey.AGE to FactValue.NumberValue(20.0, "years"),
                FactKey.ENROLLED to FactValue.BooleanValue(true),
                FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(false),
                FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(false),
                FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(6.0, "months"),
            ),
            opportunities = opportunities,
            savedOpportunityIds = setOf("oecd-internship-rolling", "cern-openlab-summer-student-2027"),
            constraints = PlanningConstraints(
                horizonDays = 90,
                maxEffort = EffortLevel.VERY_HIGH,
                maxCost = CostBand.UNKNOWN,
                prioritizeSaved = false,
            ),
            today = LocalDate(2026, 10, 21),
        )

        val availability = frontier.filter { action ->
            action.template.target is ActionTarget.OpportunityRuleTarget &&
                action.template.targetFactKey == FactKey.AVAILABLE_FULL_PERIOD
        }
        assertEquals(4, availability.size)
        availability.forEach { action ->
            val target = assertNotNull(action.template.target as? ActionTarget.OpportunityRuleTarget)
            assertEquals(listOf(target.opportunityId), action.improvedOpportunityIds)
            assertEquals(listOf(target.opportunityId), action.timingUnverifiedOpportunityIds)
            assertTrue(action.newlyOpenOpportunityIds.isEmpty())
        }
        assertEquals(2, availability.sumOf { it.improvedSavedOpportunityIds.size })
    }
}

private fun profile(vararg values: Pair<FactKey, FactValue>): ProfileSnapshot = ProfileSnapshot(
    facts = values.associate { (key, value) ->
        key to ProfileFact(
            id = key.name.lowercase(),
            key = key,
            value = value,
            actionability = when (key) {
                FactKey.AGE -> Actionability.IMMUTABLE

                FactKey.ENROLLED,
                FactKey.DEGREE_COMPLETED,
                FactKey.EXPERIENCE_MONTHS,
                -> Actionability.TIME_DEPENDENT

                FactKey.AVAILABLE_FULL_PERIOD -> Actionability.CONTROLLABLE

                else -> Actionability.UNKNOWN
            },
        )
    },
)

private fun RuleNode.flatten(): List<RuleNode> = when (this) {
    is RuleNode.All -> listOf(this) + children.flatMap(RuleNode::flatten)

    is RuleNode.Any -> listOf(this) + children.flatMap(RuleNode::flatten)

    is RuleNode.Atomic,
    is RuleNode.ManualCheck,
    -> listOf(this)
}
