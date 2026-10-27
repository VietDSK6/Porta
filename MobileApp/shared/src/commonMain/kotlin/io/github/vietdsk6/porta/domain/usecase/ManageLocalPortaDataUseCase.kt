package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.domain.model.ProfileFact

/** Coordinates the two user-visible deletion scopes without hiding what each scope retains. */
class ManageLocalPortaDataUseCase(
    private val profileRepository: ProfileRepository,
    private val planRepository: PlanRepository,
    private val savedOpportunityRepository: SavedOpportunityRepository,
    private val opportunityPackRepository: OpportunityPackRepository,
    private val userPreferences: UserPreferences,
    private val bundledOpportunityPackProvider: BundledOpportunityPackProvider,
) {
    suspend fun clearProfileAndPlans(
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = runCatching {
        planRepository.clearForProfile(profileId).getOrThrow()
        profileRepository.clearProfile(profileId).getOrThrow()
    }

    suspend fun resetAllLocalData(): Result<Unit> = runCatching {
        planRepository.clearAll().getOrThrow()
        profileRepository.clearAll().getOrThrow()
        savedOpportunityRepository.clearAll().getOrThrow()
        opportunityPackRepository.clearAll().getOrThrow()
        opportunityPackRepository.importPack(bundledOpportunityPackProvider.rawJson()).getOrThrow()
        userPreferences.clear()
    }
}
