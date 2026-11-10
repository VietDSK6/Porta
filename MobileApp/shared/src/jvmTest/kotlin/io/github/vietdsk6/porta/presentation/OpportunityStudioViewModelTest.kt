@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.vietdsk6.porta.presentation

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.vietdsk6.porta.data.BackgroundExecutor
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.source.local.AppDatabase
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackParser
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackValidator
import io.github.vietdsk6.porta.data.source.local.pack.OpportunityStudioDraftCompiler
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioMetadataField
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioRuleTextField
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioUiEvent
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioViewModel
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpportunityStudioViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: OpportunityPackRepository
    private lateinit var fileManager: RecordingFileManager
    private lateinit var viewModel: OpportunityStudioViewModel

    @BeforeTest
    fun setUp() = runTest(dispatcher) {
        Dispatchers.setMain(dispatcher)
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(dispatcher)
            .build()
        val codec = OpportunityPackCodec()
        val parser = OpportunityPackParser(codec, OpportunityPackValidator())
        repository = OpportunityPackRepository(
            database.opportunityPackDao(),
            parser,
            BackgroundExecutor(dispatcher),
        )
        repository.importPack(BundledOpportunityPackProvider(codec).rawJson()).getOrThrow()
        fileManager = RecordingFileManager()
        viewModel = OpportunityStudioViewModel(
            opportunityPackRepository = repository,
            draftCompiler = OpportunityStudioDraftCompiler(codec, parser),
            fileManager = fileManager,
            currentLocalDateProvider = CurrentLocalDateProvider { LocalDate(2026, 11, 4) },
        )
        advanceUntilIdle()
    }

    @AfterTest
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `valid draft exports candidate without changing active pack`() = runTest(dispatcher) {
        val activeBefore = repository.exportActive().getOrThrow()
        assertFalse(viewModel.uiState.value.canExport)

        makeDraftValid()
        val candidate = assertNotNull(viewModel.uiState.value.rawJson)
        assertTrue(viewModel.uiState.value.canExport)
        assertTrue(candidate.contains("new-opportunity"))

        viewModel.onUiEvent(OpportunityStudioUiEvent.ExportDraft)
        advanceUntilIdle()

        assertEquals("porta-opportunity-pack-draft", fileManager.suggestedName)
        assertEquals("json", fileManager.extension)
        assertEquals(candidate, fileManager.content)
        assertEquals(activeBefore, repository.exportActive().getOrThrow())
        assertTrue(viewModel.uiState.value.message.orEmpty().contains("Pack inspector"))
    }

    @Test
    fun `cancelled save does not report a false success`() = runTest(dispatcher) {
        fileManager.result = Result.success(false)
        makeDraftValid()

        viewModel.onUiEvent(OpportunityStudioUiEvent.ExportDraft)
        advanceUntilIdle()

        assertNotNull(fileManager.content)
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `invalid edits recompile live and block export`() = runTest(dispatcher) {
        makeDraftValid()
        assertTrue(viewModel.uiState.value.canExport)

        viewModel.onUiEvent(
            OpportunityStudioUiEvent.MetadataChanged(
                OpportunityStudioMetadataField.SOURCE_URL,
                "http://not-official.example",
            ),
        )

        assertFalse(viewModel.uiState.value.canExport)
        assertNull(viewModel.uiState.value.rawJson)
        assertTrue(viewModel.uiState.value.diagnostics.any { it.path.endsWith("sourceUrl") })
    }

    private fun makeDraftValid() {
        listOf(
            OpportunityStudioMetadataField.TITLE to "Example scholarship",
            OpportunityStudioMetadataField.PROVIDER to "Example University",
            OpportunityStudioMetadataField.CYCLE to "2027",
            OpportunityStudioMetadataField.SOURCE_URL to "https://example.edu/scholarship",
        ).forEach { (field, value) ->
            viewModel.onUiEvent(OpportunityStudioUiEvent.MetadataChanged(field, value))
        }
        viewModel.onUiEvent(
            OpportunityStudioUiEvent.RuleTextChanged(
                ruleIndex = 0,
                field = OpportunityStudioRuleTextField.EXPLANATION,
                value = "Applicants must be enrolled.",
            ),
        )
        viewModel.onUiEvent(
            OpportunityStudioUiEvent.RuleTextChanged(
                ruleIndex = 0,
                field = OpportunityStudioRuleTextField.SOURCE_NOTE,
                value = "Eligibility · enrollment",
            ),
        )
    }

    private class RecordingFileManager : FileManager {
        var suggestedName: String? = null
        var extension: String? = null
        var content: String? = null
        var result: Result<Boolean> = Result.success(true)

        override suspend fun saveTextByPickingUpLocation(
            suggestedName: String,
            extension: String,
            content: String,
        ): Result<Boolean> {
            this.suggestedName = suggestedName
            this.extension = extension
            this.content = content
            return result
        }
    }
}
