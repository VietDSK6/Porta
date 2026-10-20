package io.github.vietdsk6.porta.domain.usecase

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
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
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

        val frontier = engine.build(
            profile = profile,
            opportunities = opportunities,
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints,
            today = today,
        )

        assertTrue(frontier.isEmpty())
    }

    @Test
    fun `constraints and deadline feasibility exclude impossible milestones`() {
        val expensive = template(
            id = "ielts-expensive",
            key = FactKey.IELTS_SCORE,
            target = FactValue.NumberValue(7.0, "band"),
            durationDays = 60,
            effort = EffortLevel.HIGH,
            cost = CostBand.HIGH,
        )
        val opportunities = listOf(
            opportunity(
                id = "deadline-too-close",
                key = FactKey.IELTS_SCORE,
                required = FactValue.NumberValue(7.0, "band"),
                actualOperator = Operator.GREATER_THAN_OR_EQUAL,
                template = expensive,
                deadline = Deadline.Known(LocalDate(2026, 11, 1)),
            ),
        )
        val profile = profile(FactKey.IELTS_SCORE, FactValue.NumberValue(6.5, "band"))

        val strictFrontier = engine.build(
            profile = profile,
            opportunities = opportunities,
            savedOpportunityIds = emptySet(),
            constraints = PlanningConstraints(
                horizonDays = 30,
                maxEffort = EffortLevel.MEDIUM,
                maxCost = CostBand.MEDIUM,
            ),
            today = today,
        )
        val permissiveFrontier = engine.build(
            profile = profile,
            opportunities = opportunities,
            savedOpportunityIds = emptySet(),
            constraints = permissiveConstraints,
            today = today,
        )

        assertTrue(strictFrontier.isEmpty())
        assertTrue(permissiveFrontier.isEmpty())
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

    private fun fact(key: FactKey, value: FactValue): ProfileFact = ProfileFact(
        id = key.name,
        key = key,
        value = value,
        actionability = Actionability.CONTROLLABLE,
    )
}
