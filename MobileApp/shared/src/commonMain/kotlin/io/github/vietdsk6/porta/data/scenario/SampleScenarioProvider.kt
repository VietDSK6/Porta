package io.github.vietdsk6.porta.data.scenario

import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import kotlinx.datetime.LocalDate

/** The first-use lesson uses only the clearly fictional five-opportunity sample pack. */
data class SampleThresholdLesson(
    val beforeScore: Double,
    val afterScore: Double,
    val changedOpportunityTitles: List<String>,
    val unchangedOpportunityTitles: List<String>,
)

/** Keeps the fictional lesson and bundled-pack identity out of presentation code. */
class SampleScenarioProvider {
    val profile: ProfileSnapshot get() = PortaSampleData.profile
    val opportunities: List<Opportunity> get() = PortaSampleData.opportunities
    val referenceDate: LocalDate get() = PortaSampleData.referenceDate
    val judgeDemoSavedOpportunityIds: Set<String> get() = PortaSampleData.judgeDemoSavedOpportunityIds
    val judgeDemoHorizonDays: Int get() = PortaSampleData.JUDGE_DEMO_HORIZON_DAYS
    val thresholdLesson: SampleThresholdLesson by lazy {
        val evaluator = EligibilityEvaluator()
        val before = PortaSampleData.profile
        val beforeScore = (before.facts.getValue(FactKey.IELTS_SCORE).value as FactValue.NumberValue).amount
        val target = PortaSampleData.opportunities.first().actionTemplates.first().targetValue as FactValue.NumberValue
        val after = before.withOverrides(
            mapOf(FactKey.IELTS_SCORE to target),
        )
        val changed = mutableListOf<String>()
        val unchanged = mutableListOf<String>()
        PortaSampleData.opportunities.forEach { opportunity ->
            val beforeState = evaluator.evaluate(before, opportunity, PortaSampleData.referenceDate).state
            val afterState = evaluator.evaluate(after, opportunity, PortaSampleData.referenceDate).state
            when {
                beforeState != EligibilityState.OPEN && afterState == EligibilityState.OPEN -> changed += opportunity.title
                beforeState == EligibilityState.OPEN && afterState == EligibilityState.OPEN -> unchanged += opportunity.title
            }
        }
        SampleThresholdLesson(
            beforeScore = beforeScore,
            afterScore = target.amount,
            changedOpportunityTitles = changed,
            unchangedOpportunityTitles = unchanged,
        )
    }

    fun persistedDemoFacts(updatedAtEpochMillis: Long): List<ProfileFact> = PortaSampleData.persistedDemoFacts(updatedAtEpochMillis)

    fun isPersistedDemoProfile(profile: ProfileSnapshot): Boolean = PortaSampleData.isPersistedDemoProfile(profile)

    fun isLegacySyntheticPack(id: String): Boolean = id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID

    fun isBundledPack(id: String): Boolean = id == BundledOpportunityPackProvider.PACK_ID
}
