package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.fingerprint
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class EligibilityEvaluatorTest {
    private val evaluator = EligibilityEvaluator()
    private val today = LocalDate(2026, 10, 20)

    @Test
    fun `all gives fail precedence over review and unknown`() {
        val opportunity = opportunity(
            rule = RuleNode.All(
                id = "all",
                children = listOf(
                    atomic(
                        id = "gpa",
                        key = FactKey.GPA,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = FactValue.NumberValue(3.5, "4.0 scale"),
                    ),
                    RuleNode.ManualCheck("essay", "Essay must be reviewed by the provider."),
                    atomic(
                        id = "ielts",
                        key = FactKey.IELTS_SCORE,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = FactValue.NumberValue(7.0, "band"),
                    ),
                ),
            ),
        )
        val receipt = evaluator.evaluate(
            profile = profile(FactKey.GPA to FactValue.NumberValue(3.0, "4.0 scale")),
            opportunity = opportunity,
            today = today,
        )

        assertEquals(RuleResult.FAIL, receipt.rootResult)
        assertEquals(EligibilityState.HAS_GAPS, receipt.state)
    }

    @Test
    fun `any gives pass precedence over review and unknown`() {
        val opportunity = opportunity(
            rule = RuleNode.Any(
                id = "any",
                children = listOf(
                    atomic(
                        id = "degree",
                        key = FactKey.DEGREE_COMPLETED,
                        operator = Operator.EQUALS,
                        expected = FactValue.BooleanValue(true),
                    ),
                    RuleNode.ManualCheck("portfolio", "Portfolio quality requires manual review."),
                    atomic(
                        id = "experience",
                        key = FactKey.EXPERIENCE_MONTHS,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = FactValue.NumberValue(12.0, "months"),
                    ),
                ),
            ),
        )
        val receipt = evaluator.evaluate(
            profile = profile(FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(true)),
            opportunity = opportunity,
            today = today,
        )

        assertEquals(RuleResult.PASS, receipt.rootResult)
        assertEquals(EligibilityState.OPEN, receipt.state)
    }

    @Test
    fun `missing fact and incompatible units remain unknown`() {
        val missing = evaluator.evaluate(
            profile = profile(),
            opportunity = opportunity(
                rule = atomic(
                    id = "ielts",
                    key = FactKey.IELTS_SCORE,
                    operator = Operator.GREATER_THAN_OR_EQUAL,
                    expected = FactValue.NumberValue(7.0, "band"),
                ),
            ),
            today = today,
        )
        val incompatible = evaluator.evaluate(
            profile = profile(FactKey.IELTS_SCORE to FactValue.NumberValue(100.0, "TOEFL")),
            opportunity = opportunity(
                rule = atomic(
                    id = "ielts",
                    key = FactKey.IELTS_SCORE,
                    operator = Operator.GREATER_THAN_OR_EQUAL,
                    expected = FactValue.NumberValue(7.0, "band"),
                ),
            ),
            today = today,
        )

        assertEquals(RuleResult.UNKNOWN, missing.rootResult)
        assertEquals(EligibilityState.NEEDS_INFO, missing.state)
        assertEquals(RuleResult.UNKNOWN, incompatible.rootResult)
        assertEquals(EligibilityState.NEEDS_INFO, incompatible.state)
    }

    @Test
    fun `manual checks never become automatic pass or fail`() {
        val receipt = evaluator.evaluate(
            profile = profile(),
            opportunity = opportunity(
                rule = RuleNode.ManualCheck(
                    id = "review",
                    explanation = "The provider reviews leadership quality.",
                ),
            ),
            today = today,
        )

        assertEquals(RuleResult.REVIEW, receipt.rootResult)
        assertEquals(EligibilityState.NEEDS_INFO, receipt.state)
    }

    @Test
    fun `actionable failure remains visible beside immutable failure`() {
        val opportunity = opportunity(
            rule = RuleNode.All(
                id = "mixed-failures",
                children = listOf(
                    atomic(
                        id = "citizenship",
                        key = FactKey.NATIONALITY,
                        operator = Operator.EQUALS,
                        expected = FactValue.TextValue("eligible-country"),
                        actionability = Actionability.IMMUTABLE,
                    ),
                    atomic(
                        id = "ielts",
                        key = FactKey.IELTS_SCORE,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = FactValue.NumberValue(7.0, "band"),
                    ),
                ),
            ),
        )

        val receipt = evaluator.evaluate(
            profile = profile(
                FactKey.NATIONALITY to FactValue.TextValue("other-country"),
                FactKey.IELTS_SCORE to FactValue.NumberValue(6.5, "band"),
            ),
            opportunity = opportunity,
            today = today,
        )

        assertEquals(RuleResult.FAIL, receipt.rootResult)
        assertEquals(EligibilityState.HAS_GAPS, receipt.state)
    }

    @Test
    fun `manual review with missing deadline stays information needed`() {
        val receipt = evaluator.evaluate(
            profile = profile(),
            opportunity = opportunity(
                rule = RuleNode.ManualCheck("manual", "Confirm the provider-specific condition."),
                deadline = Deadline.Missing,
            ),
            today = today,
        )

        assertEquals(RuleResult.REVIEW, receipt.rootResult)
        assertEquals(EligibilityState.NEEDS_INFO, receipt.state)
    }

    @Test
    fun `soft criteria never change deterministic assessment`() {
        val rule = atomic(
            id = "enrolled",
            key = FactKey.ENROLLED,
            operator = Operator.EQUALS,
            expected = FactValue.BooleanValue(true),
        )
        val opportunity = opportunity(rule).copy(
            softCriteria = listOf("Leadership potential", "Competitive portfolio"),
        )

        val receipt = evaluator.evaluate(
            profile = profile(FactKey.ENROLLED to FactValue.BooleanValue(true)),
            opportunity = opportunity,
            today = today,
        )

        assertEquals(RuleResult.PASS, receipt.rootResult)
        assertEquals(EligibilityState.OPEN, receipt.state)
    }

    @Test
    fun `deadline is open through its date and closed afterward`() {
        val rule = atomic(
            id = "enrolled",
            key = FactKey.ENROLLED,
            operator = Operator.EQUALS,
            expected = FactValue.BooleanValue(true),
        )
        val profile = profile(FactKey.ENROLLED to FactValue.BooleanValue(true))
        val onDeadline = evaluator.evaluate(
            profile = profile,
            opportunity = opportunity(rule = rule, deadline = Deadline.Known(today)),
            today = today,
        )
        val afterDeadline = evaluator.evaluate(
            profile = profile,
            opportunity = opportunity(
                rule = rule,
                deadline = Deadline.Known(LocalDate(2026, 10, 19)),
            ),
            today = today,
        )

        assertEquals(EligibilityState.OPEN, onDeadline.state)
        assertEquals(EligibilityState.CLOSED, afterDeadline.state)
    }

    @Test
    fun `matching scoped assertion applies only while its rule fingerprint is current`() {
        val rule = atomic(
            id = "availability",
            key = FactKey.AVAILABLE_FULL_PERIOD,
            operator = Operator.EQUALS,
            expected = FactValue.BooleanValue(true),
        ).copy(actionTemplateId = "confirm-availability")
        val target = ActionTarget.OpportunityRuleTarget(
            opportunityId = "synthetic-opportunity",
            ruleId = rule.id,
            ruleFingerprint = rule.fingerprint(),
            factKey = rule.factKey,
            value = FactValue.BooleanValue(true),
        )
        val opportunity = opportunity(rule).copy(
            actionTemplates = listOf(
                ActionTemplate(
                    id = "confirm-availability",
                    title = "Confirm this programme period",
                    target = target,
                    targetActionability = Actionability.CONTROLLABLE,
                    minimumDurationDays = 1,
                    effort = EffortLevel.VERY_LOW,
                    costBand = CostBand.FREE,
                ),
            ),
        )
        val ruleRef = RuleRef(opportunity.id, rule.id)
        val matching = ProfileSnapshot(
            ruleAssertions = mapOf(
                ruleRef to RuleAssertion(
                    ruleRef = ruleRef,
                    ruleFingerprint = rule.fingerprint(),
                    factKey = rule.factKey,
                    value = FactValue.BooleanValue(true),
                ),
            ),
        )
        val stale = matching.copy(
            ruleAssertions = mapOf(
                ruleRef to matching.ruleAssertions.getValue(ruleRef).copy(
                    ruleFingerprint = "stale-fingerprint",
                ),
            ),
        )

        assertEquals(EligibilityState.OPEN, evaluator.evaluate(matching, opportunity, today).state)
        assertEquals(EligibilityState.NEEDS_INFO, evaluator.evaluate(stale, opportunity, today).state)
    }

    @Test
    fun `global fact does not satisfy a rule that requires a scoped assertion`() {
        val rule = atomic(
            id = "availability",
            key = FactKey.AVAILABLE_FULL_PERIOD,
            operator = Operator.EQUALS,
            expected = FactValue.BooleanValue(true),
        ).copy(actionTemplateId = "confirm-availability")
        val opportunity = opportunity(rule).copy(
            actionTemplates = listOf(
                ActionTemplate(
                    id = "confirm-availability",
                    title = "Confirm this programme period",
                    target = ActionTarget.OpportunityRuleTarget(
                        opportunityId = "synthetic-opportunity",
                        ruleId = rule.id,
                        ruleFingerprint = rule.fingerprint(),
                        factKey = rule.factKey,
                        value = FactValue.BooleanValue(true),
                    ),
                    targetActionability = Actionability.CONTROLLABLE,
                    minimumDurationDays = 1,
                    effort = EffortLevel.VERY_LOW,
                    costBand = CostBand.FREE,
                ),
            ),
        )

        val receipt = evaluator.evaluate(
            profile = profile(FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(true)),
            opportunity = opportunity,
            today = today,
        )

        assertEquals(RuleResult.UNKNOWN, receipt.rootResult)
        assertEquals(EligibilityState.NEEDS_INFO, receipt.state)
    }

    private fun profile(vararg facts: Pair<FactKey, FactValue>): ProfileSnapshot = ProfileSnapshot(
        facts = facts.associate { (key, value) ->
            key to ProfileFact(
                id = key.name,
                key = key,
                value = value,
                actionability = Actionability.CONTROLLABLE,
            )
        },
    )

    private fun atomic(
        id: String,
        key: FactKey,
        operator: Operator,
        expected: FactValue,
        actionability: Actionability = Actionability.CONTROLLABLE,
    ): RuleNode.Atomic = RuleNode.Atomic(
        id = id,
        factKey = key,
        operator = operator,
        expected = expected,
        explanation = "Synthetic test rule.",
        actionability = actionability,
    )

    private fun opportunity(
        rule: RuleNode,
        deadline: Deadline = Deadline.Known(LocalDate(2027, 6, 1)),
    ): Opportunity = Opportunity(
        id = "synthetic-opportunity",
        title = "Synthetic opportunity",
        provider = "Test provider",
        category = OpportunityCategory.SCHOLARSHIP,
        cycle = "test-only",
        sourceUrl = "https://example.invalid/test-only",
        lastCheckedOn = today,
        deadline = deadline,
        eligibilityRule = rule,
    )
}
