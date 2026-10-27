@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.domain.usecase

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.RuleAssertionRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.entity.OpportunityPackEntity
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.data.source.preferences.FakeUserPreferences
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ManageLocalPortaDataUseCaseTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var profileRepository: ProfileRepository
    private lateinit var planRepository: PlanRepository
    private lateinit var savedOpportunityRepository: SavedOpportunityRepository
    private lateinit var opportunityPackRepository: OpportunityPackRepository
    private lateinit var ruleAssertionRepository: RuleAssertionRepository
    private lateinit var userPreferences: FakeUserPreferences
    private lateinit var useCase: ManageLocalPortaDataUseCase

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        val backgroundExecutor = BackgroundExecutor(dispatcher)
        val codec = OpportunityPackCodec()
        profileRepository = ProfileRepository(
            profileFactDao = database.profileFactDao(),
            ruleAssertionDao = database.ruleAssertionDao(),
            backgroundExecutor = backgroundExecutor,
        )
        planRepository = PlanRepository(database.planItemDao(), backgroundExecutor)
        savedOpportunityRepository = SavedOpportunityRepository(database.savedOpportunityDao(), backgroundExecutor)
        opportunityPackRepository = OpportunityPackRepository(
            opportunityPackDao = database.opportunityPackDao(),
            parser = OpportunityPackParser(codec, OpportunityPackValidator()),
            backgroundExecutor = backgroundExecutor,
        )
        ruleAssertionRepository = RuleAssertionRepository(database.ruleAssertionDao(), backgroundExecutor)
        userPreferences = FakeUserPreferences()
        useCase = ManageLocalPortaDataUseCase(
            profileRepository = profileRepository,
            planRepository = planRepository,
            savedOpportunityRepository = savedOpportunityRepository,
            opportunityPackRepository = opportunityPackRepository,
            userPreferences = userPreferences,
            bundledOpportunityPackProvider = BundledOpportunityPackProvider(codec),
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `clear profile removes facts assertions and plans but retains other Porta data`() = runTest(dispatcher) {
        seedAllData()

        useCase.clearProfileAndPlans().getOrThrow()

        val profile = profileRepository.getProfile().getOrThrow()
        assertTrue(profile.facts.isEmpty())
        assertTrue(profile.ruleAssertions.isEmpty())
        assertTrue(profileRepository.getProfile(OTHER_PROFILE_ID).getOrThrow().facts.isNotEmpty())
        assertNull(planRepository.observeCurrentPlan().first())
        assertNotNull(planRepository.observeCurrentPlan(OTHER_PROFILE_ID).first())
        assertEquals(setOf("saved-door"), savedOpportunityRepository.observeSavedIds().first())
        assertEquals(setOf("other-saved-door"), savedOpportunityRepository.observeSavedIds(OTHER_PROFILE_ID).first())
        assertEquals("custom-pack", database.opportunityPackDao().getActive()?.packId)
        assertEquals(90, userPreferences.getInt(UserPreferences.KEY_FRONTIER_HORIZON_DAYS))
        assertEquals("scholarships", userPreferences.getString(UserPreferences.KEY_OPPORTUNITY_GOAL))
    }

    @Test
    fun `reset all removes local state and restores only the bundled catalog`() = runTest(dispatcher) {
        seedAllData()

        useCase.resetAllLocalData().getOrThrow()

        val profile = profileRepository.getProfile().getOrThrow()
        assertTrue(profile.facts.isEmpty())
        assertTrue(profile.ruleAssertions.isEmpty())
        val otherProfile = profileRepository.getProfile(OTHER_PROFILE_ID).getOrThrow()
        assertTrue(otherProfile.facts.isEmpty())
        assertTrue(otherProfile.ruleAssertions.isEmpty())
        assertNull(planRepository.observeCurrentPlan().first())
        assertNull(planRepository.observeCurrentPlan(OTHER_PROFILE_ID).first())
        assertTrue(savedOpportunityRepository.observeSavedIds().first().isEmpty())
        assertTrue(savedOpportunityRepository.observeSavedIds(OTHER_PROFILE_ID).first().isEmpty())
        assertNull(userPreferences.getInt(UserPreferences.KEY_FRONTIER_HORIZON_DAYS))
        assertNull(userPreferences.getString(UserPreferences.KEY_OPPORTUNITY_GOAL))
        val storedPacks = database.opportunityPackDao().observeAll().first()
        assertEquals(1, storedPacks.size)
        assertEquals(BundledOpportunityPackProvider.PACK_ID, storedPacks.single().packId)
        assertTrue(storedPacks.single().isActive)
        assertNotNull(opportunityPackRepository.getActive().getOrThrow())
    }

    private suspend fun seedAllData() {
        profileRepository.saveFacts(
            listOf(
                ProfileFact(
                    id = "default:gpa",
                    key = FactKey.GPA,
                    value = FactValue.NumberValue(3.6, "4.0 scale"),
                    actionability = Actionability.CONTROLLABLE,
                ),
            ),
        ).getOrThrow()
        profileRepository.saveFacts(
            listOf(
                ProfileFact(
                    id = "$OTHER_PROFILE_ID:gpa",
                    profileId = OTHER_PROFILE_ID,
                    key = FactKey.GPA,
                    value = FactValue.NumberValue(8.5, "10.0 scale"),
                    actionability = Actionability.CONTROLLABLE,
                ),
            ),
        ).getOrThrow()
        ruleAssertionRepository.saveAssertions(
            listOf(
                RuleAssertion(
                    ruleRef = RuleRef("programme", "availability"),
                    ruleFingerprint = "sha256:availability",
                    factKey = FactKey.AVAILABLE_FULL_PERIOD,
                    value = FactValue.BooleanValue(true),
                ),
            ),
        ).getOrThrow()
        ruleAssertionRepository.saveAssertions(
            listOf(
                RuleAssertion(
                    profileId = OTHER_PROFILE_ID,
                    ruleRef = RuleRef("other-programme", "availability"),
                    ruleFingerprint = "sha256:other-availability",
                    factKey = FactKey.AVAILABLE_FULL_PERIOD,
                    value = FactValue.BooleanValue(true),
                ),
            ),
        ).getOrThrow()
        planRepository.replaceCurrentPlan(
            PlanItem(
                id = "plan",
                actionTemplateId = "raise-ielts",
                title = "Raise IELTS",
                targetFactKey = FactKey.IELTS_SCORE,
                targetValue = FactValue.NumberValue(7.0, "band"),
                expectedDurationDays = 45,
                effort = EffortLevel.MEDIUM,
                costBand = CostBand.LOW,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            ),
        ).getOrThrow()
        planRepository.replaceCurrentPlan(
            PlanItem(
                id = "other-plan",
                profileId = OTHER_PROFILE_ID,
                actionTemplateId = "other-action",
                title = "Other plan",
                targetFactKey = FactKey.IELTS_SCORE,
                targetValue = FactValue.NumberValue(7.0, "band"),
                effort = EffortLevel.LOW,
                costBand = CostBand.FREE,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            ),
        ).getOrThrow()
        savedOpportunityRepository.setSaved("saved-door", isSaved = true).getOrThrow()
        savedOpportunityRepository.setSaved(
            "other-saved-door",
            isSaved = true,
            profileId = OTHER_PROFILE_ID,
        ).getOrThrow()
        database.opportunityPackDao().upsert(
            OpportunityPackEntity(
                id = "custom-pack:1",
                packId = "custom-pack",
                name = "Imported catalog",
                publisher = "Test publisher",
                schemaVersion = 2,
                version = 1,
                rawJson = "{}",
                lastCheckedOn = "2026-10-20",
                importedAtEpochMillis = 1L,
                isActive = true,
            ),
        )
        userPreferences.putInt(UserPreferences.KEY_FRONTIER_HORIZON_DAYS, 90)
        userPreferences.putString(UserPreferences.KEY_OPPORTUNITY_GOAL, "scholarships")
    }

    private companion object {
        const val OTHER_PROFILE_ID = "other-profile"
    }
}
