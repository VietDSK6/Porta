package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.ScenarioOverrides
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenarioEvaluatorTest {
    private val today = LocalDate(2026, 11, 11)
    private val rule = RuleNode.Atomic(
        id = "availability",
        factKey = FactKey.AVAILABLE_FULL_PERIOD,
        operator = Operator.EQUALS,
        expected = FactValue.BooleanValue(true),
        explanation = "Confirm availability for this programme.",
        actionability = Actionability.CONTROLLABLE,
    )
    private val opportunity = Opportunity(
        id = "programme",
        title = "Synthetic programme",
        provider = "Test provider",
        category = OpportunityCategory.INTERNSHIP,
        cycle = "test-only",
        sourceUrl = "https://example.invalid/programme",
        lastCheckedOn = today,
        deadline = Deadline.Known(LocalDate(2027, 6, 1)),
        eligibilityRule = rule,
    )

    @Test
    fun `missing scoped rules are ignored while existing assertions still apply`() {
        val profile = ProfileSnapshot()
        val result = ScenarioEvaluator(EligibilityEvaluator()).evaluate(
            profile = profile,
            overrides = ScenarioOverrides(
                ruleAssertions = mapOf(
                    RuleRef("removed-programme", "availability") to FactValue.BooleanValue(true),
                    RuleRef("programme", "removed-rule") to FactValue.BooleanValue(true),
                    RuleRef("programme", "availability") to FactValue.BooleanValue(true),
                ),
            ),
            opportunities = listOf(opportunity),
            today = today,
        )

        assertEquals(RuleResult.PASS, result.simulated.getValue("programme").rootResult)
        assertTrue(profile.ruleAssertions.isEmpty())
    }

    @Test
    fun `only missing scoped rules leave the scenario unchanged`() {
        val result = ScenarioEvaluator(EligibilityEvaluator()).evaluate(
            profile = ProfileSnapshot(),
            overrides = ScenarioOverrides(
                ruleAssertions = mapOf(RuleRef("programme", "removed-rule") to FactValue.BooleanValue(true)),
            ),
            opportunities = listOf(opportunity),
            today = today,
        )

        assertEquals(result.baseline, result.simulated)
        assertTrue(result.deltas.isEmpty())
        assertTrue(result.impacts.isEmpty())
    }
}
