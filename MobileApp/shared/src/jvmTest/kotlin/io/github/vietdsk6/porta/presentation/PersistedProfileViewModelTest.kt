@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsViewModel
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierViewModel
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailViewModel
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupUiEvent
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupViewModel
import io.github.vietdsk6.porta.presentation.screens.profilesetup.StudyLevelOption
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewUiEvent
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PersistedProfileViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: ProfileRepository
    private lateinit var planRepository: PlanRepository
    private lateinit var opportunityPackRepository: OpportunityPackRepository
    private val eligibilityEvaluator = EligibilityEvaluator()
    private val scenarioEvaluator = ScenarioEvaluator(eligibilityEvaluator)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        repository = ProfileRepository(
            profileFactDao = database.profileFactDao(),
            backgroundExecutor = BackgroundExecutor(dispatcher),
        )
        planRepository = PlanRepository(
            planItemDao = database.planItemDao(),
            backgroundExecutor = BackgroundExecutor(dispatcher),
        )
        opportunityPackRepository = OpportunityPackRepository(
            opportunityPackDao = database.opportunityPackDao(),
            parser = OpportunityPackParser(OpportunityPackCodec(), OpportunityPackValidator()),
            backgroundExecutor = BackgroundExecutor(dispatcher),
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `doors switch from sample facts to the persisted profile`() = runTest(dispatcher) {
        saveIelts(7.0)

        val viewModel = DoorsViewModel(eligibilityEvaluator, repository, opportunityPackRepository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSampleMode)
        assertEquals(5, viewModel.uiState.value.openCount)
    }

    @Test
    fun `doors react when the persisted profile changes`() = runTest(dispatcher) {
        saveIelts(6.5)
        val viewModel = DoorsViewModel(eligibilityEvaluator, repository, opportunityPackRepository)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.openCount)

        saveIelts(7.0)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSampleMode)
        assertEquals(5, viewModel.uiState.value.openCount)
    }

    @Test
    fun `doors return to sample mode when the profile is cleared`() = runTest(dispatcher) {
        saveIelts(7.0)
        val viewModel = DoorsViewModel(eligibilityEvaluator, repository, opportunityPackRepository)
        advanceUntilIdle()

        repository.clearProfile().getOrThrow()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSampleMode)
        assertEquals(1, viewModel.uiState.value.openCount)
    }

    @Test
    fun `doors and frontier use the sourced bundled catalog`() = runTest(dispatcher) {
        opportunityPackRepository.importPack(
            BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson(),
        ).getOrThrow()
        savePlanningProfile()

        val doors = DoorsViewModel(eligibilityEvaluator, repository, opportunityPackRepository)
        val frontier = FrontierViewModel(
            actionFrontierEngine = ActionFrontierEngine(eligibilityEvaluator, scenarioEvaluator),
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            planRepository = planRepository,
        )
        advanceUntilIdle()

        assertFalse(doors.uiState.value.isSampleCatalog)
        assertEquals(15, doors.uiState.value.catalogDoorCount)
        assertTrue(doors.uiState.value.doors.any { it.title == "Kotlin Student Coding Competition" })
        assertFalse(frontier.uiState.value.isSampleCatalog)
        assertEquals(4, frontier.uiState.value.actions.single().improvedCount)
        assertEquals("Reserve the full required programme period", frontier.uiState.value.actions.single().title)
    }

    @Test
    fun `profile setup persists and removes optional door facts explicitly`() = runTest(dispatcher) {
        val viewModel = ProfileSetupViewModel(repository)
        advanceUntilIdle()

        viewModel.onUiEvent(ProfileSetupUiEvent.StudyLevelChanged(StudyLevelOption.UNDERGRADUATE))
        viewModel.onUiEvent(ProfileSetupUiEvent.FieldOfStudyChanged("Computer Science"))
        viewModel.onUiEvent(ProfileSetupUiEvent.NationalityChanged("VN"))
        viewModel.onUiEvent(ProfileSetupUiEvent.GpaChanged("3.6"))
        viewModel.onUiEvent(ProfileSetupUiEvent.IeltsChanged("6.5"))
        viewModel.onUiEvent(ProfileSetupUiEvent.AgeChanged("20"))
        viewModel.onUiEvent(ProfileSetupUiEvent.ExperienceMonthsChanged("6"))
        viewModel.onUiEvent(ProfileSetupUiEvent.DegreeCompletedChanged(false))
        viewModel.onUiEvent(ProfileSetupUiEvent.AvailableFullPeriodChanged(false))
        viewModel.onUiEvent(ProfileSetupUiEvent.Save)
        advanceUntilIdle()

        val saved = repository.getProfile().getOrThrow()
        assertEquals(FactValue.NumberValue(3.6, "4.0 scale"), saved.facts[FactKey.GPA]?.value)
        assertEquals(FactValue.NumberValue(20.0, "years"), saved.facts[FactKey.AGE]?.value)
        assertEquals(FactValue.NumberValue(6.0, "months"), saved.facts[FactKey.EXPERIENCE_MONTHS]?.value)
        assertEquals(FactValue.BooleanValue(false), saved.facts[FactKey.DEGREE_COMPLETED]?.value)
        assertEquals(FactValue.BooleanValue(false), saved.facts[FactKey.AVAILABLE_FULL_PERIOD]?.value)

        viewModel.onUiEvent(ProfileSetupUiEvent.AgeChanged(""))
        viewModel.onUiEvent(ProfileSetupUiEvent.ExperienceMonthsChanged(""))
        viewModel.onUiEvent(ProfileSetupUiEvent.DegreeCompletedChanged(null))
        viewModel.onUiEvent(ProfileSetupUiEvent.AvailableFullPeriodChanged(null))
        viewModel.onUiEvent(ProfileSetupUiEvent.Save)
        advanceUntilIdle()

        val updated = repository.getProfile().getOrThrow()
        assertNull(updated.facts[FactKey.AGE])
        assertNull(updated.facts[FactKey.EXPERIENCE_MONTHS])
        assertNull(updated.facts[FactKey.DEGREE_COMPLETED])
        assertNull(updated.facts[FactKey.AVAILABLE_FULL_PERIOD])
    }

    @Test
    fun `receipt reads the actual persisted value`() = runTest(dispatcher) {
        saveIelts(7.0)

        val viewModel = OpportunityDetailViewModel(
            opportunityId = "aurora",
            eligibilityEvaluator = eligibilityEvaluator,
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSampleProfile)
        assertEquals("7.0 band", viewModel.uiState.value.rules.single().actualLabel)
    }

    @Test
    fun `frontier and scenario use persisted facts without mutating them`() = runTest(dispatcher) {
        saveIelts(6.5)
        val frontier = FrontierViewModel(
            actionFrontierEngine = ActionFrontierEngine(eligibilityEvaluator, scenarioEvaluator),
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            planRepository = planRepository,
        )
        advanceUntilIdle()

        assertFalse(frontier.uiState.value.isSampleMode)
        val action = frontier.uiState.value.actions.first()
        assertEquals("6.5 band", action.currentLabel)
        assertEquals(action.id, frontier.uiState.value.selectedActionId)
        assertTrue(action.doorComparisons.any { it.previewStateLabel == "Open" })
        val scenario = ScenarioPreviewViewModel(
            actionId = action.id,
            scenarioEvaluator = scenarioEvaluator,
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            planRepository = planRepository,
        )
        advanceUntilIdle()

        assertFalse(scenario.uiState.value.isSampleProfile)
        assertEquals("6.5 band", scenario.uiState.value.beforeLabel)
        assertEquals(4, scenario.uiState.value.openedDoors.size)
        assertEquals(
            FactValue.NumberValue(6.5, "band"),
            repository.getProfile().getOrThrow().facts[FactKey.IELTS_SCORE]?.value,
        )
    }

    @Test
    fun `real profile scenario saves one local plan`() = runTest(dispatcher) {
        saveIelts(6.5)
        val scenario = ScenarioPreviewViewModel(
            actionId = "raise-ielts-aurora",
            scenarioEvaluator = scenarioEvaluator,
            profileRepository = repository,
            opportunityPackRepository = opportunityPackRepository,
            planRepository = planRepository,
        )
        advanceUntilIdle()

        scenario.onUiEvent(ScenarioPreviewUiEvent.SavePlan)
        advanceUntilIdle()

        assertTrue(scenario.uiState.value.isSaved)
        assertEquals("raise-ielts-aurora", planRepository.observeCurrentPlan().first()?.actionTemplateId)
    }

    private suspend fun saveIelts(value: Double) {
        assertTrue(
            repository.saveFacts(
                listOf(
                    ProfileFact(
                        id = "default:ielts_score",
                        key = FactKey.IELTS_SCORE,
                        value = FactValue.NumberValue(value, "band"),
                        actionability = Actionability.CONTROLLABLE,
                    ),
                ),
            ).isSuccess,
        )
    }

    private suspend fun savePlanningProfile() {
        val values = listOf(
            FactKey.AGE to FactValue.NumberValue(20.0, "years"),
            FactKey.ENROLLED to FactValue.BooleanValue(true),
            FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(false),
            FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(6.0, "months"),
            FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(false),
        )
        assertTrue(
            repository.saveFacts(
                values.map { (key, value) ->
                    ProfileFact(
                        id = "default:${key.name.lowercase()}",
                        key = key,
                        value = value,
                        actionability = when (key) {
                            FactKey.AGE -> Actionability.IMMUTABLE
                            FactKey.AVAILABLE_FULL_PERIOD -> Actionability.CONTROLLABLE
                            else -> Actionability.TIME_DEPENDENT
                        },
                    )
                },
            ).isSuccess,
        )
    }
}
