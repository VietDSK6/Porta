package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.DoorStateDelta
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.ScenarioResult
import kotlinx.datetime.LocalDate

class ScenarioEvaluator(
    private val eligibilityEvaluator: EligibilityEvaluator,
) {
    fun evaluate(
        profile: ProfileSnapshot,
        overrides: Map<FactKey, FactValue>,
        opportunities: List<Opportunity>,
        today: LocalDate,
    ): ScenarioResult {
        val simulatedProfile = profile.withOverrides(overrides)
        val baseline = opportunities.associate { opportunity ->
            opportunity.id to eligibilityEvaluator.evaluate(profile, opportunity, today)
        }
        val simulated = opportunities.associate { opportunity ->
            opportunity.id to eligibilityEvaluator.evaluate(simulatedProfile, opportunity, today)
        }
        val deltas = opportunities.mapNotNull { opportunity ->
            val before = baseline.getValue(opportunity.id).state
            val after = simulated.getValue(opportunity.id).state
            if (before == after) {
                null
            } else {
                DoorStateDelta(
                    opportunityId = opportunity.id,
                    before = before,
                    after = after,
                )
            }
        }

        return ScenarioResult(
            baseline = baseline,
            simulated = simulated,
            deltas = deltas,
        )
    }
}
