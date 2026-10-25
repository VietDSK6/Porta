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
import io.github.vietdsk6.porta.domain.model.FrontierAction
import io.github.vietdsk6.porta.domain.model.FrontierExclusionReason
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.fingerprint
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActionFrontierEngineTest {
    private val today = LocalDate(2026, 10, 20)
    private val eligibilityEvaluator = EligibilityEvaluator()
    private val scenarioEvaluator = ScenarioEvaluator(eligibilityEvaluator)
    private val engine = ActionFrontierEngine(eligibilityEvaluator, scenarioEvaluator)

    @Test
    fun `IELTS milestone opens exactly four doors without mutating actual profile`() {
        val actualProfile = profile(
            key = FactKey.IELTS_SCORE,
            value = FactValue.NumberValue(6.5, "band"),
        )
        val opportunities = (1..4).map { index ->
            ieltsOpportunity(id = "locked-$index", requiredScore = 7.0)
        } + ieltsOpportunity(id = "already-open", requiredScore = 6.0)

        val frontier = engine.build(
            profile = actualProfile,
            opportunities = opportunities,
            savedOpportunityIds = setOf("locked-1", "locked-2"),
            constraints = permissiveConstraints,
            today = today,
        )

        assertEquals(1, frontier.size)
        assertEquals(listOf("locked-1", "locked-2", "locked-3", "locked-4"), frontier.single().newlyOpenOpportunityIds)
        assertEquals(2, frontier.single().newlyOpenSavedOpportunityIds.size)
        assertEquals(
            FactValue.NumberValue(6.5, "band"),
            actualProfile.facts.getValue(FactKey.IELTS_SCORE).value,
        )
        assertEquals(
            EligibilityState.OPEN,
            frontier.single().scenario.simulated.getValue("locked-1").state,
        )
    }

    @Test
    fun `immutable and context-only changes are never recommended`() {
        val opportunities = listOf(
            opportunity(
                id = "age-gate",
                key = FactKey.AGE,
                required = FactValue.NumberValue(21.0, "years"),
                actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                template = template(
                    id = "change-age",
                    key = FactKey.AGE,
                    target = FactValue.NumberValue(21.0, "years"),
                    actionability = Actionability.IMMUTABLE,
                ),
            ),
            opportunity(
                id = "nationality-gate",
                key = FactKey.NATIONALITY,
                required = FactValue.EnumValue("VN"),
                actualOperator = Operator.EQUALS,
                template = template(
                    id = "change-nationality",
                    key = FactKey.NATIONALITY,
                    target = FactValue.EnumValue("VN"),
                    actionability = Actionability.CONTEXT_ONLY,
                ),
            ),
        )
        val profile = ProfileSnapshot(
            facts = mapOf(
                FactKey.AGE to fact(FactKey.AGE, FactValue.NumberValue(19.0, "years")),
                FactKey.NATIONALITY to fact(FactKey.NATIONALITY, FactValue.EnumValue("US")),
            ),
        )

        val evaluation = engine.evaluate(
            profile = profile,
            opportunities = opportunities,
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints,
            today = today,
        )

        assertTrue(evaluation.actions.isEmpty())
        assertTrue(evaluation.exclusions.isEmpty())
    }

    @Test
    fun `all violated user constraints are diagnosed together`() {
        val expensive = template(
            id = "ielts-expensive",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
            durationDays = 60,
            effort = EffortLevel.HIGH,
            cost = CostBand.HIGH,
        )
        val opportunity = opportunity(
            id = "expensive-door",
            key = FactKey.IELTS_SCORE,
            required = FactValue.NumberValue(7.0, "band"),
            actualOperator = Operator.GREATER_THAN_OR_EQUAL,
            template = expensive,
        )
        val profile = profile(FactKey.IELTS_SCORE, FactValue.NumberValue(6.5, "band"))

        val evaluation = engine.evaluate(
            profile = profile,
            opportunities = listOf(opportunity),
            savedOpportunityIds = emptySet(),
            constraints = PlanningConstraints(
                horizonDays = 30,
                maxEffort = EffortLevel.MEDIUM,
                maxCost = CostBand.MEDIUM,
            ),
            today = today,
        )

        assertTrue(evaluation.actions.isEmpty())
        assertEquals(
            setOf(
                FrontierExclusionReason.BEYOND_HORIZON,
                FrontierExclusionReason.ABOVE_MAX_EFFORT,
                FrontierExclusionReason.ABOVE_MAX_COST,
            ),
            evaluation.exclusions.single().reasons,
        )
    }

    @Test
    fun `known deadline accepts exact duration boundary and diagnoses one day short`() {
        val exact = template(
            id = "exact-boundary",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
            durationDays = 30,
        )
        val tooLate = template(
            id = "one-day-too-late",
            key = FactKey.GPA,
            target = FactValue.NumberValue(4.0, "4.0 scale"),
            durationDays = 30,
        )

        val evaluation = engine.evaluate(
            profile = profileOf(
                FactKey.IELTS_SCORE to FactValue.NumberValue(6.5, "band"),
                FactKey.GPA to FactValue.NumberValue(3.5, "4.0 scale"),
            ),
            opportunities = listOf(
                opportunity(
                    id = "exact-door",
                    key = FactKey.IELTS_SCORE,
                    required = FactValue.NumberValue(7.0, "band"),
                    actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                    template = exact,
                    deadline = Deadline.Known(LocalDate(2026, 11, 19)),
                ),
                opportunity(
                    id = "missed-door",
                    key = FactKey.GPA,
                    required = FactValue.NumberValue(4.0, "4.0 scale"),
                    actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                    template = tooLate,
                    deadline = Deadline.Known(LocalDate(2026, 11, 18)),
                ),
            ),
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints,
            today = today,
        )

        assertEquals(listOf("exact-boundary"), evaluation.actions.map { it.template.id })
        assertEquals(
            setOf(FrontierExclusionReason.MISSES_KNOWN_DEADLINES),
            evaluation.exclusions.single().reasons,
        )
    }

    @Test
    fun `template that does not resolve its encoded gap has no verified impact`() {
        val insufficientTarget = template(
            id = "insufficient-ielts",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
        )

        val evaluation = engine.evaluate(
            profile = profile(FactKey.IELTS_SCORE, FactValue.NumberValue(6.5, "band")),
            opportunities = listOf(
                opportunity(
                    id = "ielts-eight",
                    key = FactKey.IELTS_SCORE,
                    required = FactValue.NumberValue(8.0, "band"),
                    actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                    template = insufficientTarget,
                ),
            ),
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints,
            today = today,
        )

        assertTrue(evaluation.actions.isEmpty())
        assertEquals(
            setOf(FrontierExclusionReason.NO_VERIFIED_IMPACT),
            evaluation.exclusions.single().reasons,
        )
    }

    @Test
    fun `feasible candidate dominated on impact and resources is diagnosed`() {
        val dominant = template(
            id = "dominant-action",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
            durationDays = 10,
            effort = EffortLevel.LOW,
            cost = CostBand.FREE,
        )
        val dominated = template(
            id = "dominated-action",
            key = FactKey.GPA,
            target = FactValue.NumberValue(4.0, "4.0 scale"),
            durationDays = 20,
            effort = EffortLevel.MEDIUM,
            cost = CostBand.LOW,
        )

        val evaluation = engine.evaluate(
            profile = profileOf(
                FactKey.IELTS_SCORE to FactValue.NumberValue(6.5, "band"),
                FactKey.GPA to FactValue.NumberValue(3.5, "4.0 scale"),
            ),
            opportunities = listOf(
                opportunity(
                    id = "language-a",
                    key = FactKey.IELTS_SCORE,
                    required = FactValue.NumberValue(7.0, "band"),
                    actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                    template = dominant,
                ),
                opportunity(
                    id = "language-b",
                    key = FactKey.IELTS_SCORE,
                    required = FactValue.NumberValue(7.0, "band"),
                    actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                    template = dominant.copy(id = "dominant-copy"),
                ),
                opportunity(
                    id = "academic",
                    key = FactKey.GPA,
                    required = FactValue.NumberValue(4.0, "4.0 scale"),
                    actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                    template = dominated,
                ),
            ),
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints.copy(prioritizeSaved = false),
            today = today,
        )

        assertEquals(listOf("dominant-action"), evaluation.actions.map { it.template.id })
        assertEquals("dominated-action", evaluation.exclusions.single().template.id)
        assertEquals(
            setOf(FrontierExclusionReason.DOMINATED),
            evaluation.exclusions.single().reasons,
        )
    }

    @Test
    fun `saved metrics affect dominance only when saved priority is enabled`() {
        val broad = template(
            id = "broad-action",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
        )
        val saved = template(
            id = "saved-action",
            key = FactKey.GPA,
            target = FactValue.NumberValue(4.0, "4.0 scale"),
        )
        val opportunities = listOf(
            opportunity(
                id = "broad-a",
                key = FactKey.IELTS_SCORE,
                required = FactValue.NumberValue(7.0, "band"),
                actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                template = broad,
            ),
            opportunity(
                id = "broad-b",
                key = FactKey.IELTS_SCORE,
                required = FactValue.NumberValue(7.0, "band"),
                actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                template = broad.copy(id = "broad-copy"),
            ),
            opportunity(
                id = "saved-door",
                key = FactKey.GPA,
                required = FactValue.NumberValue(4.0, "4.0 scale"),
                actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                template = saved,
            ),
        )
        val profile = profileOf(
            FactKey.IELTS_SCORE to FactValue.NumberValue(6.5, "band"),
            FactKey.GPA to FactValue.NumberValue(3.5, "4.0 scale"),
        )

        val prioritized = engine.evaluate(
            profile = profile,
            opportunities = opportunities,
            savedOpportunityIds = setOf("saved-door"),
            constraints = permissiveConstraints.copy(prioritizeSaved = true),
            today = today,
        )
        val unprioritized = engine.evaluate(
            profile = profile,
            opportunities = opportunities,
            savedOpportunityIds = setOf("saved-door"),
            constraints = permissiveConstraints.copy(prioritizeSaved = false),
            today = today,
        )

        assertEquals(listOf("saved-action", "broad-action"), prioritized.actions.map { it.template.id })
        assertTrue(prioritized.exclusions.isEmpty())
        assertEquals(listOf("broad-action"), unprioritized.actions.map { it.template.id })
        assertEquals("saved-action", unprioritized.exclusions.single().template.id)
        assertEquals(
            setOf(FrontierExclusionReason.DOMINATED),
            unprioritized.exclusions.single().reasons,
        )
    }

    @Test
    fun `ranking prefers newly open doors before total improvements`() {
        val opensDoor = template(
            id = "opens-one",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
        )
        val improvesDoors = template(
            id = "improves-two",
            key = FactKey.EXPERIENCE_MONTHS,
            target = FactValue.NumberValue(12.0, "months"),
        )

        val evaluation = engine.evaluate(
            profile = profileOf(
                FactKey.IELTS_SCORE to FactValue.NumberValue(6.5, "band"),
                FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(6.0, "months"),
            ),
            opportunities = listOf(
                opportunity(
                    id = "opens",
                    key = FactKey.IELTS_SCORE,
                    required = FactValue.NumberValue(7.0, "band"),
                    actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                    template = opensDoor,
                ),
                reviewOpportunity(
                    id = "improves-a",
                    key = FactKey.EXPERIENCE_MONTHS,
                    required = FactValue.NumberValue(12.0, "months"),
                    template = improvesDoors,
                ),
                reviewOpportunity(
                    id = "improves-b",
                    key = FactKey.EXPERIENCE_MONTHS,
                    required = FactValue.NumberValue(12.0, "months"),
                    template = improvesDoors,
                ),
            ),
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints.copy(prioritizeSaved = false),
            today = today,
        )

        assertEquals(listOf("opens-one", "improves-two"), evaluation.actions.map { it.template.id })
        assertEquals(listOf(1, 0), evaluation.actions.map { it.newlyOpenOpportunityIds.size })
        assertEquals(listOf(1, 2), evaluation.actions.map { it.improvedOpportunityIds.size })
    }

    @Test
    fun `equivalent candidates use stable template id ordering`() {
        val zAction = template(
            id = "z-action",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
        )
        val aAction = template(
            id = "a-action",
            key = FactKey.GPA,
            target = FactValue.NumberValue(4.0, "4.0 scale"),
        )

        val evaluation = engine.evaluate(
            profile = profileOf(
                FactKey.IELTS_SCORE to FactValue.NumberValue(6.5, "band"),
                FactKey.GPA to FactValue.NumberValue(3.5, "4.0 scale"),
            ),
            opportunities = listOf(
                opportunity(
                    id = "z-door",
                    key = FactKey.IELTS_SCORE,
                    required = FactValue.NumberValue(7.0, "band"),
                    actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                    template = zAction,
                ),
                opportunity(
                    id = "a-door",
                    key = FactKey.GPA,
                    required = FactValue.NumberValue(4.0, "4.0 scale"),
                    actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                    template = aAction,
                ),
            ),
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints.copy(prioritizeSaved = false),
            today = today,
        )

        assertEquals(listOf("a-action", "z-action"), evaluation.actions.map { it.template.id })
        assertTrue(evaluation.exclusions.isEmpty())
    }

    @Test
    fun `scoped availability actions never leak impact across opportunities`() {
        val opportunities = listOf(
            scopedAvailabilityOpportunity("programme-a"),
            scopedAvailabilityOpportunity("programme-b"),
        )

        val evaluation = engine.evaluate(
            profile = profile(
                FactKey.AVAILABLE_FULL_PERIOD,
                FactValue.BooleanValue(false),
            ),
            opportunities = opportunities,
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints.copy(prioritizeSaved = false),
            today = today,
        )

        assertEquals(2, evaluation.actions.size)
        assertEquals(
            listOf(listOf("programme-a"), listOf("programme-b")),
            evaluation.actions.map(FrontierAction::improvedOpportunityIds),
        )
    }

    @Test
    fun `stale scoped target fingerprint has no verified impact`() {
        val opportunity = scopedAvailabilityOpportunity("programme-a")
        val staleTemplate = opportunity.actionTemplates.single().copy(
            target = (opportunity.actionTemplates.single().target as ActionTarget.OpportunityRuleTarget).copy(
                ruleFingerprint = "stale-fingerprint",
            ),
        )

        val evaluation = engine.evaluate(
            profile = ProfileSnapshot(),
            opportunities = listOf(opportunity.copy(actionTemplates = listOf(staleTemplate))),
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints,
            today = today,
        )

        assertTrue(evaluation.actions.isEmpty())
        assertEquals(
            setOf(FrontierExclusionReason.NO_VERIFIED_IMPACT),
            evaluation.exclusions.single().reasons,
        )
    }

    @Test
    fun `detail candidate ignores passed rules and actions that miss the deadline`() {
        val passedAction = template(
            id = "passed-language",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
        )
        val unmetAction = template(
            id = "unmet-gpa",
            key = FactKey.GPA,
            target = FactValue.NumberValue(4.0, "4.0 scale"),
        )
        val opportunity = Opportunity(
            id = "two-rules",
            title = "Two-rule test",
            provider = "Test provider",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "test-only",
            sourceUrl = "https://example.invalid/two-rules",
            lastCheckedOn = today,
            deadline = Deadline.Known(LocalDate(2027, 6, 1)),
            eligibilityRule = RuleNode.All(
                id = "two-rules-root",
                children = listOf(
                    RuleNode.Atomic(
                        id = "language-rule",
                        factKey = FactKey.IELTS_SCORE,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = FactValue.NumberValue(7.0, "band"),
                        explanation = "Language check",
                        actionability = Actionability.CONTROLLABLE,
                        actionTemplateId = passedAction.id,
                    ),
                    RuleNode.Atomic(
                        id = "gpa-rule",
                        factKey = FactKey.GPA,
                        operator = Operator.GREATER_THAN_OR_EQUAL,
                        expected = FactValue.NumberValue(4.0, "4.0 scale"),
                        explanation = "GPA check",
                        actionability = Actionability.CONTROLLABLE,
                        actionTemplateId = unmetAction.id,
                    ),
                ),
            ),
            actionTemplates = listOf(passedAction, unmetAction),
        )
        val profile = profileOf(
            FactKey.IELTS_SCORE to FactValue.NumberValue(7.0, "band"),
            FactKey.GPA to FactValue.NumberValue(3.5, "4.0 scale"),
        )

        assertEquals("unmet-gpa", engine.relevantActionFor(profile, opportunity, today)?.id)
        assertNull(
            engine.relevantActionFor(
                profile = profile,
                opportunity = opportunity.copy(deadline = Deadline.Known(LocalDate(2026, 10, 25))),
                today = today,
            ),
        )
    }

    private val permissiveConstraints = PlanningConstraints(
        horizonDays = 365,
        maxEffort = EffortLevel.VERY_HIGH,
        maxCost = CostBand.UNKNOWN,
    )

    private fun ieltsOpportunity(id: String, requiredScore: Double): Opportunity {
        val action = template(
            id = "raise-ielts-$id",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
            durationDays = 45,
        )
        return opportunity(
            id = id,
            key = FactKey.IELTS_SCORE,
            required = FactValue.NumberValue(requiredScore, "band"),
            actualOperator = Operator.GREATER_THAN_OR_EQUAL,
            template = action,
        )
    }

    private fun opportunity(
        id: String,
        key: FactKey,
        required: FactValue,
        actualOperator: Operator,
        template: ActionTemplate,
        deadline: Deadline = Deadline.Known(LocalDate(2027, 6, 1)),
    ): Opportunity = Opportunity(
        id = id,
        title = "Synthetic $id",
        provider = "Test provider",
        category = OpportunityCategory.SCHOLARSHIP,
        cycle = "test-only",
        sourceUrl = "https://example.invalid/$id",
        lastCheckedOn = today,
        deadline = deadline,
        eligibilityRule = RuleNode.Atomic(
            id = "$id-rule",
            factKey = key,
            operator = actualOperator,
            expected = required,
            explanation = "Synthetic test rule.",
            actionability = template.targetActionability,
            actionTemplateId = template.id,
        ),
        actionTemplates = listOf(template),
    )

    private fun reviewOpportunity(
        id: String,
        key: FactKey,
        required: FactValue,
        template: ActionTemplate,
    ): Opportunity = Opportunity(
        id = id,
        title = "Synthetic $id",
        provider = "Test provider",
        category = OpportunityCategory.SCHOLARSHIP,
        cycle = "test-only",
        sourceUrl = "https://example.invalid/$id",
        lastCheckedOn = today,
        deadline = Deadline.Known(LocalDate(2027, 6, 1)),
        eligibilityRule = RuleNode.All(
            id = "$id-all",
            children = listOf(
                RuleNode.Atomic(
                    id = "$id-rule",
                    factKey = key,
                    operator = Operator.GREATER_THAN_OR_EQUAL,
                    expected = required,
                    explanation = "Synthetic test rule.",
                    actionability = template.targetActionability,
                    actionTemplateId = template.id,
                ),
                RuleNode.ManualCheck(
                    id = "$id-review",
                    explanation = "Provider review is required.",
                ),
            ),
        ),
        actionTemplates = listOf(template),
    )

    private fun scopedAvailabilityOpportunity(id: String): Opportunity {
        val templateId = "confirm-$id"
        val rule = RuleNode.Atomic(
            id = "$id-availability",
            factKey = FactKey.AVAILABLE_FULL_PERIOD,
            operator = Operator.EQUALS,
            expected = FactValue.BooleanValue(true),
            explanation = "Confirm availability for this programme's exact dates.",
            actionability = Actionability.CONTROLLABLE,
            actionTemplateId = templateId,
        )
        val template = ActionTemplate(
            id = templateId,
            title = "Confirm $id availability",
            target = ActionTarget.OpportunityRuleTarget(
                opportunityId = id,
                ruleId = rule.id,
                ruleFingerprint = rule.fingerprint(),
                factKey = rule.factKey,
                value = FactValue.BooleanValue(true),
            ),
            targetActionability = Actionability.CONTROLLABLE,
            minimumDurationDays = 1,
            effort = EffortLevel.VERY_LOW,
            costBand = CostBand.FREE,
        )
        return Opportunity(
            id = id,
            title = "Synthetic $id",
            provider = "Test provider",
            category = OpportunityCategory.SCHOLARSHIP,
            cycle = "test-only",
            sourceUrl = "https://example.invalid/$id",
            lastCheckedOn = today,
            deadline = Deadline.Known(LocalDate(2027, 6, 1)),
            eligibilityRule = rule,
            actionTemplates = listOf(template),
        )
    }

    private fun template(
        id: String,
        key: FactKey,
        target: FactValue,
        actionability: Actionability = Actionability.CONTROLLABLE,
        durationDays: Int = 30,
        effort: EffortLevel = EffortLevel.MEDIUM,
        cost: CostBand = CostBand.LOW,
    ): ActionTemplate = ActionTemplate(
        id = id,
        title = "Reach $target",
        targetFactKey = key,
        targetValue = target,
        targetActionability = actionability,
        minimumDurationDays = durationDays,
        effort = effort,
        costBand = cost,
    )

    private fun profile(key: FactKey, value: FactValue): ProfileSnapshot = ProfileSnapshot(
        facts = mapOf(key to fact(key, value)),
    )

    private fun profileOf(vararg values: Pair<FactKey, FactValue>): ProfileSnapshot = ProfileSnapshot(
        facts = values.associate { (key, value) -> key to fact(key, value) },
    )

    private fun fact(key: FactKey, value: FactValue): ProfileFact = ProfileFact(
        id = key.name,
        key = key,
        value = value,
        actionability = Actionability.CONTROLLABLE,
    )
}
