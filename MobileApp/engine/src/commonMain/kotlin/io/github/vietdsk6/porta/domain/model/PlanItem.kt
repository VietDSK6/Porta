package io.github.vietdsk6.porta.domain.model

data class PlanItem(
    val id: String,
    val profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    val actionTemplateId: String,
    val title: String,
    val target: ActionTarget,
    val status: PlanStatus = PlanStatus.PLANNED,
    val targetDateEpochDays: Long? = null,
    val note: String? = null,
    val expectedDurationDays: Int? = null,
    val effort: EffortLevel,
    val costBand: CostBand,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val routeId: String? = null,
    val sequence: Int? = null,
) {
    val targetFactKey: FactKey
        get() = target.factKey

    val targetValue: FactValue
        get() = target.value

    constructor(
        id: String,
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
        actionTemplateId: String,
        title: String,
        targetFactKey: FactKey,
        targetValue: FactValue,
        status: PlanStatus = PlanStatus.PLANNED,
        targetDateEpochDays: Long? = null,
        note: String? = null,
        expectedDurationDays: Int? = null,
        effort: EffortLevel,
        costBand: CostBand,
        createdAtEpochMillis: Long,
        updatedAtEpochMillis: Long,
        routeId: String? = null,
        sequence: Int? = null,
    ) : this(
        id = id,
        profileId = profileId,
        actionTemplateId = actionTemplateId,
        title = title,
        target = ActionTarget.ProfileFactTarget(
            factKey = targetFactKey,
            value = targetValue,
        ),
        status = status,
        targetDateEpochDays = targetDateEpochDays,
        note = note,
        expectedDurationDays = expectedDurationDays,
        effort = effort,
        costBand = costBand,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        routeId = routeId,
        sequence = sequence,
    )
}

enum class PlanStatus {
    PLANNED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
}
