package io.github.vietdsk6.porta.data.source.local.entity

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanItem
import kotlin.test.Test
import kotlin.test.assertEquals

class PlanItemEntityTest {
    @Test
    fun `rule scoped plan target survives persistence mapping`() {
        val plan = planItem(
            target = ActionTarget.OpportunityRuleTarget(
                opportunityId = "world-bank-treasury",
                ruleId = "programme-availability",
                ruleFingerprint = "sha256:programme-availability",
                factKey = FactKey.AVAILABLE_FULL_PERIOD,
                value = FactValue.BooleanValue(true),
            ),
        )

        assertEquals(plan, plan.toEntity().toModel())
    }

    @Test
    fun `profile fact plan target survives persistence mapping`() {
        val plan = planItem(
            target = ActionTarget.ProfileFactTarget(
                factKey = FactKey.IELTS_SCORE,
                value = FactValue.NumberValue(7.0, "band"),
            ),
        )

        assertEquals(plan, plan.toEntity().toModel())
    }

    private fun planItem(target: ActionTarget): PlanItem = PlanItem(
        id = "plan-1",
        actionTemplateId = "action-1",
        title = "Confirm the programme period",
        target = target,
        effort = EffortLevel.LOW,
        costBand = CostBand.FREE,
        createdAtEpochMillis = 10L,
        updatedAtEpochMillis = 20L,
    )
}
