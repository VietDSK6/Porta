package io.github.vietdsk6.porta.data.repository

import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.source.local.dao.PlanItemDao
import io.github.vietdsk6.porta.data.source.local.entity.toEntity
import io.github.vietdsk6.porta.data.source.local.entity.toModel
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.PlannedRoute
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlanRepository(
    private val planItemDao: PlanItemDao,
    private val backgroundExecutor: BackgroundExecutor,
) {
    fun observePlans(profileId: String = ProfileFact.DEFAULT_PROFILE_ID): Flow<List<PlanItem>> = planItemDao.observeForProfile(profileId).map { entities -> entities.map { it.toModel() } }

    fun observeCurrentPlan(profileId: String = ProfileFact.DEFAULT_PROFILE_ID): Flow<PlanItem?> = observePlans(profileId).map { plans ->
        // A route is one ordered plan. A completed first step must remain addressable while
        // later steps are still PLANNED; status priority alone would skip its confirmation.
        plans.filter { it.routeId != null && it.sequence != null }
            .minWithOrNull(compareBy<PlanItem> { it.sequence }.thenBy { it.id })
            ?: plans.maxWithOrNull(
                compareBy<PlanItem> { it.status.priority() }
                    .thenBy { it.updatedAtEpochMillis }
                    .thenBy { it.id },
            )
    }

    /** Never substitutes another step when the selected plan was replaced or removed. */
    fun observePlan(
        id: String,
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Flow<PlanItem?> = observePlans(profileId).map { plans -> plans.firstOrNull { it.id == id } }

    suspend fun replaceCurrentPlan(plan: PlanItem): Result<Unit> = backgroundExecutor.execute {
        planItemDao.replaceForProfile(plan.toEntity())
        Result.success(Unit)
    }

    /** Replaces the active plan with an ordered route in one database transaction. */
    suspend fun saveRoute(
        route: PlannedRoute,
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        require(route.steps.isNotEmpty()) { "Cannot save an empty route." }
        val savedAt = nowEpochMillis()
        val plans = route.steps.mapIndexed { index, step ->
            PlanItem(
                id = "${route.id}:$index",
                profileId = profileId,
                actionTemplateId = step.template.id,
                title = step.template.title,
                target = step.template.target,
                targetDateEpochDays = step.endDate.toEpochDays().toLong(),
                expectedDurationDays = step.template.minimumDurationDays,
                effort = step.template.effort,
                costBand = step.template.costBand,
                createdAtEpochMillis = savedAt,
                updatedAtEpochMillis = savedAt,
                routeId = route.id,
                sequence = index,
            )
        }
        planItemDao.replaceRouteForProfile(profileId, plans.map(PlanItem::toEntity))
        Result.success(Unit)
    }

    suspend fun updateStatus(
        id: String,
        status: PlanStatus,
        updatedAtEpochMillis: Long = nowEpochMillis(),
    ): Result<Unit> = backgroundExecutor.execute {
        planItemDao.updateStatus(id, status.name, updatedAtEpochMillis)
        Result.success(Unit)
    }

    suspend fun remove(id: String): Result<Unit> = backgroundExecutor.execute {
        planItemDao.deleteById(id)
        Result.success(Unit)
    }

    suspend fun clearForProfile(
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): Result<Unit> = backgroundExecutor.execute {
        planItemDao.deleteForProfile(profileId)
        Result.success(Unit)
    }

    suspend fun clearAll(): Result<Unit> = backgroundExecutor.execute {
        planItemDao.deleteAll()
        Result.success(Unit)
    }

    private fun PlanStatus.priority(): Int = when (this) {
        PlanStatus.IN_PROGRESS -> 4
        PlanStatus.PLANNED -> 3
        PlanStatus.PAUSED -> 2
        PlanStatus.COMPLETED -> 1
    }
}
