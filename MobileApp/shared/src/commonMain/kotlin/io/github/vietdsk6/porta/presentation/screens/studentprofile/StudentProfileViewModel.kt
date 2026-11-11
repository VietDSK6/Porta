package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.ProfileImportRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.data.source.local.profile.ProfileExportCodec
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.usecase.ManageLocalPortaDataUseCase
import io.github.vietdsk6.porta.presentation.screens.profilesetup.GrowthEvidenceEncoding
import io.github.vietdsk6.porta.presentation.screens.profilesetup.countryForStoredValue
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
) : ViewModel() {
    private val transientState = MutableStateFlow(StudentProfileTransientState())

    val uiState: StateFlow<StudentProfileUiState> = combine(
        profileRepository.observeProfile(),
        opportunityPackRepository.observeActive(),
        transientState,
    ) { profile, activePackResult, transient ->
        val activePack = activePackResult.getOrNull()
        buildUiState(
            profile = profile,
            opportunities = activePack?.opportunities ?: PortaSampleData.opportunities,
            isSampleCatalog = activePack == null ||
                activePack.metadata.id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID,
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
                "Porta could not read that profile copy. Your current profile is unchanged.",
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
                            message = "${fact.label} was removed. Dependent rules will now be marked unknown.",
                        )
                    }
                }
                .onFailure { showFailure("Porta could not remove that fact.") }
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
                                        "That evidence file is not stored on this device. " +
                                            "Imported profile copies include its description only.",
                                    )
                                }
                            },
                            onFailure = { showFailure("Porta could not open that evidence file.") },
                        )
                    },
                    onFailure = { showFailure("That evidence file is no longer available.") },
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
                    onFailure = { showFailure("Porta could not export the profile metadata.") },
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
                            "Profile metadata exported. No evidence files were included."
                        } else {
                            null
                        },
                    )
                }
            },
            onFailure = { showFailure("Porta could not export the profile metadata.") },
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
                showFailure("That file is not a supported Porta profile copy. Your current profile is unchanged.")
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
                            message = "Profile copy imported. Doors and next moves now use the imported facts.",
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
                                "Academic profile, programme confirmations, and plans were deleted. " +
                                    "Saved doors and preferences were kept."
                            }

                            ProfileDataAction.RESET_ALL_LOCAL_DATA -> {
                                "Porta was reset on this device. The bundled opportunity catalog was restored."
                            }
                        },
                    )
                }
                .onFailure {
                    showFailure(
                        when (action) {
                            ProfileDataAction.CLEAR_PROFILE_AND_PLANS -> "Porta could not clear the academic profile."
                            ProfileDataAction.RESET_ALL_LOCAL_DATA -> "Porta could not finish resetting local data."
                        },
                    )
                }
        }
    }

    private fun showFailure(message: String) {
        transientState.update { it.copy(isWorking = false, message = message) }
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
            isDemoProfile = PortaSampleData.isPersistedDemoProfile(profile),
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

    private fun RuleNode.factKeys(): Set<FactKey> = when (this) {
        is RuleNode.All -> children.flatMapTo(mutableSetOf()) { it.factKeys() }
        is RuleNode.Any -> children.flatMapTo(mutableSetOf()) { it.factKeys() }
        is RuleNode.Atomic -> setOf(factKey)
        is RuleNode.ManualCheck -> emptySet()
    }

    private fun FactValue.profileDisplayValue(key: FactKey): String = when {
        key == FactKey.GPA && this is FactValue.NumberValue -> {
            val scale = unit.removeSuffix(" scale").ifBlank { "4.0" }
            "${amount.compact()} / $scale"
        }

        (key == FactKey.NATIONALITY || key == FactKey.RESIDENCY) && this is FactValue.EnumValue -> {
            countryForStoredValue(value)?.displayName ?: value
        }

        this is FactValue.NumberValue -> "${amount.compact()} $unit".trim()

        else -> displayValue()
    }

    private fun Double.compact(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

    private fun Long.readableFileSize(): String = when {
        this >= BYTES_PER_MEGABYTE -> "${(this.toDouble() / BYTES_PER_MEGABYTE).oneDecimal()} MB"
        this >= BYTES_PER_KILOBYTE -> "${(this.toDouble() / BYTES_PER_KILOBYTE).oneDecimal()} KB"
        else -> "$this B"
    }

    private fun Double.oneDecimal(): String {
        val roundedTenths = (this * 10.0).toInt()
        val whole = roundedTenths / 10
        val fraction = roundedTenths % 10
        return if (fraction == 0) whole.toString() else "$whole.$fraction"
    }

    private fun FactKey.label(): String = when (this) {
        FactKey.GPA -> "GPA"
        FactKey.STUDY_LEVEL -> "Study level"
        FactKey.FIELD_OF_STUDY -> "Field of study"
        FactKey.NATIONALITY -> "Nationality"
        FactKey.RESIDENCY -> "Residency"
        FactKey.IELTS_SCORE -> "IELTS score"
        FactKey.EXPERIENCE_MONTHS -> "Experience"
        FactKey.COMMUNITY_PROJECT -> "Community project"
        FactKey.LEADERSHIP -> "Leadership"
        FactKey.ENROLLED -> "Currently enrolled"
        FactKey.AGE -> "Age"
        FactKey.DATE_OF_BIRTH -> "Date of birth"
        FactKey.DEGREE_COMPLETED -> "Degree completed"
        FactKey.AVAILABLE_FULL_PERIOD -> "Available for full period"
    }

    private fun FactProvenance.label(): String = when (this) {
        FactProvenance.SELF_REPORTED -> "Self-reported"
        FactProvenance.EVIDENCE_BACKED -> "Evidence-backed"
    }

    private fun Actionability.label(): String = when (this) {
        Actionability.CONTROLLABLE -> "Can change"
        Actionability.TIME_DEPENDENT -> "Changes with time"
        Actionability.CONTEXT_ONLY -> "Context only"
        Actionability.IMMUTABLE -> "Not an action target"
        Actionability.UNKNOWN -> "Actionability unknown"
    }

    private data class StudentProfileTransientState(
        val isWorking: Boolean = false,
        val expandedFactKey: FactKey? = null,
        val factPendingDelete: ProfileFactUiState? = null,
        val pendingDataAction: ProfileDataAction? = null,
        val pendingProfileImport: PendingProfileImport? = null,
        val profileImportError: String? = null,
        val message: String? = null,
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
        private const val BYTES_PER_KILOBYTE = 1_024L
        private const val BYTES_PER_MEGABYTE = 1_024L * 1_024L
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
