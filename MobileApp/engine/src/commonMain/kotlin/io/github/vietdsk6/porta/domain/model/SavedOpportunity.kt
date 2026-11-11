package io.github.vietdsk6.porta.domain.model

data class SavedOpportunity(
    val id: String,
    val profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    val opportunityId: String,
    val savedAtEpochMillis: Long,
)
