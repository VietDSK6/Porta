package io.github.vietdsk6.porta.data.scenario

import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import kotlinx.datetime.LocalDate

/** Keeps the fictional lesson and bundled-pack identity out of presentation code. */
class SampleScenarioProvider {
    val profile: ProfileSnapshot get() = PortaSampleData.profile
    val opportunities: List<Opportunity> get() = PortaSampleData.opportunities
    val referenceDate: LocalDate get() = PortaSampleData.referenceDate
    val judgeDemoSavedOpportunityIds: Set<String> get() = PortaSampleData.judgeDemoSavedOpportunityIds
    val judgeDemoHorizonDays: Int get() = PortaSampleData.JUDGE_DEMO_HORIZON_DAYS

    fun persistedDemoFacts(updatedAtEpochMillis: Long): List<ProfileFact> = PortaSampleData.persistedDemoFacts(updatedAtEpochMillis)

    fun isPersistedDemoProfile(profile: ProfileSnapshot): Boolean = PortaSampleData.isPersistedDemoProfile(profile)

    fun isLegacySyntheticPack(id: String): Boolean = id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID

    fun isBundledPack(id: String): Boolean = id == BundledOpportunityPackProvider.PACK_ID
}
