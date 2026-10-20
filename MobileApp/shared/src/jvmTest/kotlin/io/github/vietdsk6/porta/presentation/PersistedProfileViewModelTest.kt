@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.source.local.AppDatabase
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
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewViewModel
import kotlinx.coroutines.Dispatchers
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
import kotlin.test.assertTrue

class PersistedProfileViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: ProfileRepository
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
    }

    @AfterTest
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `doors switch from sample facts to the persisted profile`() = runTest(dispatcher) {
        saveIelts(7.0)

        val viewModel = DoorsViewModel(eligibilityEvaluator, repository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSampleMode)
        assertEquals(5, viewModel.uiState.value.openCount)
    }

    @Test
    fun `receipt reads the actual persisted value`() = runTest(dispatcher) {
        saveIelts(7.0)

        val viewModel = OpportunityDetailViewModel(
            opportunityId = "aurora",
            eligibilityEvaluator = eligibilityEvaluator,
            profileRepository = repository,
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
        )
        advanceUntilIdle()

        assertFalse(frontier.uiState.value.isSampleMode)
        val action = frontier.uiState.value.actions.first()
        val scenario = ScenarioPreviewViewModel(
            actionId = action.id,
            scenarioEvaluator = scenarioEvaluator,
            profileRepository = repository,
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
}
