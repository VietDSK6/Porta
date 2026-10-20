package io.github.vietdsk6.porta.domain.model

data class PlanItem(
    val id: String,
    val profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    val actionTemplateId: String,
    val title: String,
    val targetFactKey: FactKey,
    val targetValue: FactValue,
    val status: PlanStatus = PlanStatus.PLANNED,
    val targetDateEpochDays: Long? = null,
    val note: String? = null,
    val expectedDurationDays: Int? = null,
    val effort: EffortLevel,
    val costBand: CostBand,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

enum class PlanStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
}
