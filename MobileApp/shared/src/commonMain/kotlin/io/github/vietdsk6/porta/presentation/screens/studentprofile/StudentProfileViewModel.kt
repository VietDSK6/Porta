package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.ProfileImportRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.scenario.SampleScenarioProvider
import io.github.vietdsk6.porta.data.source.local.profile.ProfileExportCodec
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.usecase.ManageLocalPortaDataUseCase
import io.github.vietdsk6.porta.presentation.locale.AppLanguageState
import io.github.vietdsk6.porta.presentation.screens.profilesetup.GrowthEvidenceEncoding
import io.github.vietdsk6.porta.util.PlatformCapabilities
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.getPlatformCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudentProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val opportunityPackRepository: OpportunityPackRepository,
    private val manageLocalPortaData: ManageLocalPortaDataUseCase,
    private val profileExportCodec: ProfileExportCodec,
    private val profileImportRepository: ProfileImportRepository,
    private val fileManager: FileManager,
    private val platformCapabilities: PlatformCapabilities = getPlatformCapabilities(),
    private val userPreferences: UserPreferences? = null,
    private val sampleScenarioProvider: SampleScenarioProvider = SampleScenarioProvider(),
) : ViewModel() {
    private val transientState = MutableStateFlow(StudentProfileTransientState())

    init {
        loadAiKeyStatus()
    }

    val uiState: StateFlow<StudentProfileUiState> = combine(
        profileRepository.observeProfile(),
        opportunityPackRepository.observeActive(),
        transientState,
    ) { profile, activePackResult, transient ->
        val activePack = activePackResult.getOrNull()
        buildUiState(
            profile = profile,
            opportunities = activePack?.opportunities ?: sampleScenarioProvider.opportunities,
            isSampleCatalog = activePack == null ||
                sampleScenarioProvider.isLegacySyntheticPack(activePack.metadata.id),
            transient = transient,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = StudentProfileUiState(),
    )

    fun onUiEvent(event: StudentProfileUiEvent) {
        when (event) {
            is StudentProfileUiEvent.ToggleDependencies -> transientState.update { state ->
                state.copy(
                    expandedFactKey = event.key.takeUnless { it == state.expandedFactKey },
                    message = null,
                )
            }

            is StudentProfileUiEvent.DeleteFactRequested -> transientState.update {
                it.copy(factPendingDelete = event.fact, message = null)
            }

            is StudentProfileUiEvent.OpenEvidenceAttachment -> openEvidenceAttachment(event.key)

            StudentProfileUiEvent.DeleteFactCancelled -> transientState.update {
                it.copy(factPendingDelete = null)
            }

            StudentProfileUiEvent.DeleteFactConfirmed -> deletePendingFact()

            StudentProfileUiEvent.ExportMetadata -> exportMetadata()

            is StudentProfileUiEvent.ImportProfileSelected -> previewProfileImport(event)

            StudentProfileUiEvent.ImportProfileReadFailed -> showFailure(
                ProfileNotice.IMPORT_READ_FAILED,
            )

            StudentProfileUiEvent.ImportProfileCancelled -> transientState.update {
                it.copy(pendingProfileImport = null, profileImportError = null)
            }

            StudentProfileUiEvent.ImportProfileConfirmed -> confirmProfileImport()

            is StudentProfileUiEvent.DataActionRequested -> transientState.update {
                it.copy(pendingDataAction = event.action, message = null)
            }

            StudentProfileUiEvent.DataActionCancelled -> transientState.update {
                it.copy(pendingDataAction = null)
            }

            StudentProfileUiEvent.DataActionConfirmed -> confirmPendingDataAction()

            is StudentProfileUiEvent.AiKeySaveRequested -> saveAiKey(event.key)

            StudentProfileUiEvent.AiKeyDeleteRequested -> deleteAiKey()

            is StudentProfileUiEvent.LanguageChanged -> changeLanguage(event.language)

            StudentProfileUiEvent.MessageHandled -> transientState.update { it.copy(message = null) }
        }
    }

    private fun deletePendingFact() {
        val fact = transientState.value.factPendingDelete ?: return
        viewModelScope.launch {
            transientState.update { it.copy(isWorking = true, factPendingDelete = null) }
            profileRepository.deleteFact(fact.key)
                .onSuccess {
                    transientState.update {
                        it.copy(
                            isWorking = false,
                            expandedFactKey = null,
                            message = ProfileNotice.FACT_REMOVED,
                        )
                    }
                }
                .onFailure { showFailure(ProfileNotice.FACT_REMOVE_FAILED) }
        }
    }

    private fun openEvidenceAttachment(key: FactKey) {
        viewModelScope.launch {
            profileRepository.getProfile()
                .mapCatching { profile ->
                    profile.facts[key]
                        ?.evidence
                        ?.attachment
                        ?: error("No evidence attachment is available for this fact.")
                }
                .fold(
                    onSuccess = { attachment ->
                        fileManager.openEvidenceAttachment(attachment).fold(
                            onSuccess = { opened ->
                                if (!opened) {
                                    showFailure(
                                        ProfileNotice.EVIDENCE_NOT_ON_DEVICE,
                                    )
                                }
                            },
                            onFailure = { showFailure(ProfileNotice.EVIDENCE_OPEN_FAILED) },
                        )
                    },
                    onFailure = { showFailure(ProfileNotice.EVIDENCE_MISSING) },
                )
        }
    }

    private fun exportMetadata() {
        viewModelScope.launch {
            transientState.update { it.copy(isWorking = true, message = null) }
            profileRepository.getProfile()
                .mapCatching { profile ->
                    require(profile.facts.isNotEmpty()) { "There is no profile to export." }
                    profileExportCodec.encode(profile, nowEpochMillis())
                }
                .fold(
                    onSuccess = { json -> saveExport(json) },
                    onFailure = { showFailure(ProfileNotice.EXPORT_FAILED) },
                )
        }
    }

    private suspend fun saveExport(json: String) {
        fileManager.saveTextByPickingUpLocation(
            suggestedName = "porta-profile",
            extension = "json",
            content = json,
        ).fold(
            onSuccess = { wasSaved ->
                transientState.update {
                    it.copy(
                        isWorking = false,
                        message = if (wasSaved) {
                            ProfileNotice.EXPORTED
                        } else {
                            null
                        },
                    )
                }
            },
            onFailure = { showFailure(ProfileNotice.EXPORT_FAILED) },
        )
    }

    private fun previewProfileImport(event: StudentProfileUiEvent.ImportProfileSelected) {
        runCatching {
            require(platformCapabilities.supportsStudio) { "Profile import is available on Desktop." }
            require(event.encodedProfile.length <= MAX_PROFILE_IMPORT_CHARACTERS) {
                "Profile copy is too large."
            }
            val profile = profileExportCodec.decode(event.encodedProfile)
            require(profile.facts.isNotEmpty()) { "Profile copy contains no facts." }
            require(profile.facts.size <= FactKey.entries.size) { "Profile copy contains too many facts." }
            require(profile.ruleAssertions.size <= MAX_PROFILE_IMPORT_ASSERTIONS) {
                "Profile copy contains too many programme confirmations."
            }
            PendingProfileImport(
                fileName = event.fileName.take(MAX_PROFILE_IMPORT_FILE_NAME_CHARACTERS),
                profile = profile,
            )
        }.fold(
            onSuccess = { pendingImport ->
                transientState.update {
                    it.copy(
                        pendingProfileImport = pendingImport,
                        profileImportError = null,
                        message = null,
                    )
                }
            },
            onFailure = {
                showFailure(ProfileNotice.UNSUPPORTED_IMPORT)
            },
        )
    }

    private fun confirmProfileImport() {
        val pendingImport = transientState.value.pendingProfileImport ?: return
        viewModelScope.launch {
            transientState.update {
                it.copy(isWorking = true, profileImportError = null, message = null)
            }
            profileImportRepository.replaceActiveProfile(pendingImport.profile)
                .onSuccess {
                    transientState.update {
                        it.copy(
                            isWorking = false,
                            pendingProfileImport = null,
                            profileImportError = null,
                            expandedFactKey = null,
                            message = ProfileNotice.IMPORTED,
                        )
                    }
                }
                .onFailure {
                    transientState.update {
                        it.copy(
                            isWorking = false,
                            profileImportError = "Import failed. Your current profile is unchanged.",
                        )
                    }
                }
        }
    }

    private fun confirmPendingDataAction() {
        val action = transientState.value.pendingDataAction ?: return
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    isWorking = true,
                    pendingDataAction = null,
                    message = null,
                )
            }
            val result = when (action) {
                ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> manageLocalPortaData.clearProfileAndPlans()
                ProfileDataAction.RESET_ALL_LOCAL_DATA -> manageLocalPortaData.resetAllLocalData()
            }
            result
                .onSuccess {
                    transientState.value = StudentProfileTransientState(
                        message = when (action) {
                            ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> {
                                ProfileNotice.CLEARED
                            }

                            ProfileDataAction.RESET_ALL_LOCAL_DATA -> {
                                ProfileNotice.RESET
                            }
                        },
                    )
                }
                .onFailure {
                    showFailure(
                        when (action) {
                            ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> ProfileNotice.CLEAR_FAILED
                            ProfileDataAction.RESET_ALL_LOCAL_DATA -> ProfileNotice.RESET_FAILED
                        },
                    )
                }
        }
    }

    private fun showFailure(message: ProfileNotice) {
        transientState.update { it.copy(isWorking = false, message = message) }
    }

    private fun loadAiKeyStatus() {
        val preferences = userPreferences ?: return
        viewModelScope.launch {
            try {
                val saved = !preferences.getString(UserPreferences.KEY_AI_DRAFT_API_KEY).isNullOrBlank()
                val language = preferences.getString(UserPreferences.KEY_APP_LANGUAGE)?.takeIf { it == "vi" } ?: "en"
                transientState.update { it.copy(aiKeySaved = saved, selectedLanguage = language) }
            } catch (_: Exception) {
                showFailure(ProfileNotice.AI_SETTINGS_READ_FAILED)
            }
        }
    }

    private fun saveAiKey(rawKey: String) {
        val preferences = userPreferences ?: return
        val key = rawKey.trim()
        if (key.isEmpty() || key.length > MAX_AI_KEY_CHARACTERS) {
            showFailure(ProfileNotice.AI_KEY_INVALID)
            return
        }
        viewModelScope.launch {
            transientState.update { it.copy(isWorking = true, message = null) }
            try {
                preferences.putString(UserPreferences.KEY_AI_DRAFT_API_KEY, key)
                transientState.update {
                    it.copy(isWorking = false, aiKeySaved = true, message = ProfileNotice.AI_KEY_SAVED)
                }
            } catch (_: Exception) {
                showFailure(ProfileNotice.AI_KEY_SAVE_FAILED)
            }
        }
    }

    private fun deleteAiKey() {
        val preferences = userPreferences ?: return
        viewModelScope.launch {
            transientState.update { it.copy(isWorking = true, message = null) }
            try {
                preferences.remove(UserPreferences.KEY_AI_DRAFT_API_KEY)
                transientState.update {
                    it.copy(isWorking = false, aiKeySaved = false, message = ProfileNotice.AI_KEY_REMOVED)
                }
            } catch (_: Exception) {
                showFailure(ProfileNotice.AI_KEY_REMOVE_FAILED)
            }
        }
    }

    private fun changeLanguage(language: String?) {
        val preferences = userPreferences ?: return
        if (language != null && language != "en" && language != "vi") return
        val normalized = language ?: "en"
        val previous = transientState.value.selectedLanguage ?: "en"
        transientState.update { it.copy(selectedLanguage = normalized) }
        AppLanguageState.select(normalized)
        viewModelScope.launch {
            try {
                preferences.putString(UserPreferences.KEY_APP_LANGUAGE, normalized)
            } catch (_: Exception) {
                AppLanguageState.select(previous)
                transientState.update { it.copy(selectedLanguage = previous) }
                showFailure(ProfileNotice.LANGUAGE_SAVE_FAILED)
            }
        }
    }

    private fun buildUiState(
        profile: ProfileSnapshot,
        opportunities: List<Opportunity>,
        isSampleCatalog: Boolean,
        transient: StudentProfileTransientState,
    ): StudentProfileUiState {
        val facts = profile.facts.values.associate { fact ->
            val growthRecord = (fact.value as? FactValue.TextValue)
                ?.takeIf { fact.key == FactKey.COMMUNITY_PROJECT || fact.key == FactKey.LEADERSHIP }
                ?.let { GrowthEvidenceEncoding.decode(it.value) }
            fact.key to ProfileFactUiState(
                key = fact.key,
                label = fact.key.label(),
                value = growthRecord?.primary.orEmpty().ifBlank { fact.value.profileDisplayValue(fact.key) },
                detail = growthRecord?.secondary?.takeIf(String::isNotBlank),
                isGrowthEvidence = growthRecord != null,
                provenanceLabel = fact.provenance.label(),
                actionabilityLabel = fact.actionability.label(),
                evidenceLabel = fact.evidence?.let { evidence ->
                    listOfNotNull(evidence.label, evidence.reference?.takeIf(String::isNotBlank)).joinToString(" · ")
                },
                dependentDoorTitles = opportunities
                    .filter { opportunity -> opportunity.eligibilityRule.uses(fact.key) }
                    .map(Opportunity::title),
                evidenceAttachmentName = fact.evidence?.attachment?.displayName,
                evidenceAttachmentDetail = fact.evidence?.attachment?.let { attachment ->
                    listOfNotNull(
                        attachment.mimeType?.takeIf(String::isNotBlank),
                        attachment.byteSize.readableFileSize(),
                    ).joinToString(" · ")
                },
                canOpenEvidenceAttachment = fact.evidence?.attachment?.storageKey != null,
            )
        }
        val sections = PROFILE_SECTIONS.mapNotNull { section ->
            val sectionFacts = section.keys.mapNotNull(facts::get)
            section.takeIf { sectionFacts.isNotEmpty() }?.let {
                ProfileFactSectionUiState(
                    title = it.title,
                    description = it.description,
                    facts = sectionFacts,
                )
            }
        }
        val uncheckedFacts = opportunities
            .flatMap { opportunity ->
                opportunity.eligibilityRule.factKeys().map { key -> key to opportunity.title }
            }
            .filterNot { (key) -> key in facts || key == FactKey.AVAILABLE_FULL_PERIOD }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            .map { (key, doorTitles) ->
                UncheckedProfileFactUiState(
                    key = key,
                    label = key.label(),
                    dependentDoorCount = doorTitles.distinct().size,
                )
            }
            .sortedWith(
                compareByDescending<UncheckedProfileFactUiState> { it.dependentDoorCount }
                    .thenBy { it.label },
            )
        return StudentProfileUiState(
            isLoading = false,
            isWorking = transient.isWorking,
            hasRealProfile = facts.isNotEmpty(),
            isDemoProfile = sampleScenarioProvider.isPersistedDemoProfile(profile),
            factCount = facts.size,
            catalogOpportunityCount = opportunities.map(Opportunity::id).distinct().size,
            dependentOpportunityCount = referencedOpportunityCount(opportunities, facts.keys),
            sections = sections,
            uncheckedFacts = uncheckedFacts,
            isSampleCatalog = isSampleCatalog,
            expandedFactKey = transient.expandedFactKey,
            factPendingDelete = transient.factPendingDelete,
            pendingDataAction = transient.pendingDataAction,
            supportsProfileImport = platformCapabilities.supportsStudio,
            aiDraftSettingsAvailable = userPreferences != null,
            aiKeySaved = transient.aiKeySaved,
            languageSettingsAvailable = userPreferences != null,
            selectedLanguage = transient.selectedLanguage,
            pendingProfileImport = transient.pendingProfileImport?.toPreview(),
            profileImportError = transient.profileImportError,
            message = transient.message,
        )
    }

    private fun PendingProfileImport.toPreview(): ProfileImportPreviewUiState {
        val facts = profile.facts.values
            .sortedBy { it.key.name }
            .map { fact ->
                ProfileImportFactUiState(
                    key = fact.key,
                    label = fact.key.label(),
                    value = fact.value.profileDisplayValue(fact.key),
                    provenanceLabel = fact.provenance.label(),
                    hasEvidenceMetadata = fact.evidence != null,
                    isSensitive = fact.key in SENSITIVE_PROFILE_FACTS,
                )
            }
        val evidenceMetadataCount = profile.facts.values.count { it.evidence != null } +
            profile.ruleAssertions.values.count { it.evidence != null }
        return ProfileImportPreviewUiState(
            fileName = fileName,
            facts = facts,
            ruleAssertionCount = profile.ruleAssertions.size,
            evidenceMetadataCount = evidenceMetadataCount,
            sensitiveFactCount = facts.count(ProfileImportFactUiState::isSensitive),
        )
    }

    private data class StudentProfileTransientState(
        val isWorking: Boolean = false,
        val aiKeySaved: Boolean = false,
        val selectedLanguage: String? = "en",
        val expandedFactKey: FactKey? = null,
        val factPendingDelete: ProfileFactUiState? = null,
        val pendingDataAction: ProfileDataAction? = null,
        val pendingProfileImport: PendingProfileImport? = null,
        val profileImportError: String? = null,
        val message: ProfileNotice? = null,
    )

    private data class PendingProfileImport(
        val fileName: String,
        val profile: ProfileSnapshot,
    )

    private data class ProfileSectionDefinition(
        val title: String,
        val description: String,
        val keys: List<FactKey>,
    )

    companion object {
        private const val MAX_PROFILE_IMPORT_CHARACTERS = 1_000_000
        private const val MAX_PROFILE_IMPORT_ASSERTIONS = 1_000
        private const val MAX_PROFILE_IMPORT_FILE_NAME_CHARACTERS = 120
        private const val MAX_AI_KEY_CHARACTERS = 512
        private val SENSITIVE_PROFILE_FACTS = setOf(
            FactKey.NATIONALITY,
            FactKey.RESIDENCY,
            FactKey.DATE_OF_BIRTH,
            FactKey.AGE,
        )
        private val PROFILE_SECTIONS = listOf(
            ProfileSectionDefinition(
                title = "Essentials",
                description = "Your current study context.",
                keys = listOf(
                    FactKey.STUDY_LEVEL,
                    FactKey.FIELD_OF_STUDY,
                    FactKey.ENROLLED,
                ),
            ),
            ProfileSectionDefinition(
                title = "Academic record",
                description = "Measured results stay on their original scale.",
                keys = listOf(
                    FactKey.GPA,
                    FactKey.IELTS_SCORE,
                    FactKey.DEGREE_COMPLETED,
                ),
            ),
            ProfileSectionDefinition(
                title = "Experience and evidence",
                description = "Growth evidence informs planning but never passes a published check automatically.",
                keys = listOf(
                    FactKey.EXPERIENCE_MONTHS,
                    FactKey.COMMUNITY_PROJECT,
                    FactKey.LEADERSHIP,
                ),
            ),
            ProfileSectionDefinition(
                title = "Context",
                description = "Porta explains context-dependent rules but never recommends changing identity.",
                keys = listOf(
                    FactKey.NATIONALITY,
                    FactKey.RESIDENCY,
                    FactKey.DATE_OF_BIRTH,
                    FactKey.AGE,
                ),
            ),
        )
    }
}

internal fun referencedOpportunityCount(
    opportunities: List<Opportunity>,
    providedKeys: Set<FactKey>,
): Int = opportunities
    .asSequence()
    .filter { opportunity -> providedKeys.any { key -> opportunity.eligibilityRule.uses(key) } }
    .map(Opportunity::id)
    .distinct()
    .count()

private fun RuleNode.uses(key: FactKey): Boolean = when (this) {
    is RuleNode.All -> children.any { it.uses(key) }
    is RuleNode.Any -> children.any { it.uses(key) }
    is RuleNode.Atomic -> factKey == key
    is RuleNode.ManualCheck -> false
}
