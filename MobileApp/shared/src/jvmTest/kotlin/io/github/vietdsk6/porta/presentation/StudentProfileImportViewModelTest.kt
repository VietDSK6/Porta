@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileImportRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.data.source.local.profile.ProfileExportCodec
import io.github.vietdsk6.porta.data.source.preferences.FakeUserPreferences
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.usecase.ManageLocalPortaDataUseCase
import io.github.vietdsk6.porta.presentation.locale.AppLanguageState
import io.github.vietdsk6.porta.presentation.screens.studentprofile.ProfileNotice
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileUiEvent
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileViewModel
import io.github.vietdsk6.porta.util.file.FileManager
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StudentProfileImportViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var profileRepository: ProfileRepository
    private lateinit var viewModel: StudentProfileViewModel
    private lateinit var preferences: FakeUserPreferences
    private val codec = ProfileExportCodec()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        val backgroundExecutor = BackgroundExecutor(dispatcher)
        profileRepository = ProfileRepository(
            database.profileFactDao(),
            database.ruleAssertionDao(),
            backgroundExecutor,
        )
        val packCodec = OpportunityPackCodec()
        val opportunityPackRepository = OpportunityPackRepository(
            database.opportunityPackDao(),
            OpportunityPackParser(packCodec, OpportunityPackValidator()),
            backgroundExecutor,
        )
        val planRepository = PlanRepository(database.planItemDao(), backgroundExecutor)
        val savedOpportunityRepository = SavedOpportunityRepository(database.savedOpportunityDao(), backgroundExecutor)
        preferences = FakeUserPreferences()
        viewModel = StudentProfileViewModel(
            profileRepository = profileRepository,
            opportunityPackRepository = opportunityPackRepository,
            manageLocalPortaData = ManageLocalPortaDataUseCase(
                profileRepository = profileRepository,
                planRepository = planRepository,
                savedOpportunityRepository = savedOpportunityRepository,
                opportunityPackRepository = opportunityPackRepository,
                userPreferences = preferences,
                bundledOpportunityPackProvider = BundledOpportunityPackProvider(packCodec),
            ),
            profileExportCodec = codec,
            profileImportRepository = ProfileImportRepository(database.profileImportDao(), backgroundExecutor),
            fileManager = object : FileManager {
                override suspend fun saveTextByPickingUpLocation(
                    suggestedName: String,
                    extension: String,
                    content: String,
                ): Result<Boolean> = Result.success(false)
            },
            userPreferences = preferences,
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
        AppLanguageState.select(null)
        Dispatchers.resetMain()
    }

    @Test
    fun `preview and cancel do not write while confirm replaces the active profile`() = runTest(dispatcher) {
        val original = fact(FactKey.GPA, FactValue.NumberValue(3.2, "4.0 scale"))
        profileRepository.saveFacts(listOf(original)).getOrThrow()
        advanceUntilIdle()
        val imported = ProfileSnapshot(
            id = "android-profile",
            facts = mapOf(
                FactKey.IELTS_SCORE to fact(
                    FactKey.IELTS_SCORE,
                    FactValue.NumberValue(7.5, "band"),
                    profileId = "android-profile",
                ),
            ),
        )
        val encoded = codec.encode(imported, exportedAtEpochMillis = 99L)

        viewModel.onUiEvent(StudentProfileUiEvent.ImportProfileSelected("porta-profile.json", encoded))
        assertTrue(viewModel.uiState.value.supportsProfileImport)
        assertNotNull(viewModel.uiState.value.pendingProfileImport)
        assertEquals(setOf(FactKey.GPA), profileRepository.getProfile().getOrThrow().facts.keys)

        viewModel.onUiEvent(StudentProfileUiEvent.ImportProfileCancelled)
        assertNull(viewModel.uiState.value.pendingProfileImport)
        assertEquals(setOf(FactKey.GPA), profileRepository.getProfile().getOrThrow().facts.keys)

        viewModel.onUiEvent(StudentProfileUiEvent.ImportProfileSelected("porta-profile.json", encoded))
        viewModel.onUiEvent(StudentProfileUiEvent.ImportProfileConfirmed)
        advanceUntilIdle()

        val stored = profileRepository.getProfile().getOrThrow()
        assertEquals(setOf(FactKey.IELTS_SCORE), stored.facts.keys)
        assertEquals(FactValue.NumberValue(7.5, "band"), stored.facts.getValue(FactKey.IELTS_SCORE).value)
        assertNull(viewModel.uiState.value.pendingProfileImport)
        assertEquals(ProfileNotice.IMPORTED, viewModel.uiState.value.message)
    }

    @Test
    fun `AI drafting key stays local and can be removed`() = runTest(dispatcher) {
        val localValue = "local-test-value"
        viewModel.onUiEvent(StudentProfileUiEvent.AiKeySaveRequested(localValue))
        advanceUntilIdle()

        assertEquals(localValue, preferences.getString(UserPreferences.KEY_AI_DRAFT_API_KEY))
        assertTrue(viewModel.uiState.value.aiKeySaved)
        assertFalse(codec.encode(profileRepository.getProfile().getOrThrow(), 0L).contains(localValue))

        viewModel.onUiEvent(StudentProfileUiEvent.AiKeyDeleteRequested)
        advanceUntilIdle()

        assertNull(preferences.getString(UserPreferences.KEY_AI_DRAFT_API_KEY))
        assertFalse(viewModel.uiState.value.aiKeySaved)
    }

    @Test
    fun `Vietnamese choice persists locally and English can be restored`() = runTest(dispatcher) {
        assertEquals("en", viewModel.uiState.value.selectedLanguage)
        viewModel.onUiEvent(StudentProfileUiEvent.LanguageChanged("vi"))
        advanceUntilIdle()

        assertEquals("vi", preferences.getString(UserPreferences.KEY_APP_LANGUAGE))
        assertEquals("vi", viewModel.uiState.value.selectedLanguage)
        assertEquals("vi", AppLanguageState.selected)

        viewModel.onUiEvent(StudentProfileUiEvent.LanguageChanged("en"))
        advanceUntilIdle()

        assertEquals("en", preferences.getString(UserPreferences.KEY_APP_LANGUAGE))
        assertEquals("en", viewModel.uiState.value.selectedLanguage)
        assertEquals("en", AppLanguageState.selected)
    }

    private fun fact(
        key: FactKey,
        value: FactValue,
        profileId: String = ProfileFact.DEFAULT_PROFILE_ID,
    ): ProfileFact = ProfileFact(
        id = "$profileId:${key.name.lowercase()}",
        profileId = profileId,
        key = key,
        value = value,
        updatedAtEpochMillis = 42L,
    )
}
