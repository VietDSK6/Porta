package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
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
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import io.github.vietdsk6.porta.util.file.FileManager
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
    private val planRepository: PlanRepository,
    private val profileExportCodec: ProfileExportCodec,
    private val fileManager: FileManager,
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

            StudentProfileUiEvent.DeleteFactCancelled -> transientState.update {
                it.copy(factPendingDelete = null)
            }

            StudentProfileUiEvent.DeleteFactConfirmed -> deletePendingFact()

            StudentProfileUiEvent.ExportMetadata -> exportMetadata()

            StudentProfileUiEvent.ClearProfileRequested -> transientState.update {
                it.copy(isClearConfirmationVisible = true, message = null)
            }

            StudentProfileUiEvent.ClearProfileCancelled -> transientState.update {
                it.copy(isClearConfirmationVisible = false)
            }

            StudentProfileUiEvent.ClearProfileConfirmed -> clearLocalProfile()

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

    private fun clearLocalProfile() {
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    isWorking = true,
                    isClearConfirmationVisible = false,
                    message = null,
                )
            }
            val planResult = planRepository.clearForProfile()
            val profileResult = if (planResult.isSuccess) {
                profileRepository.clearProfile()
            } else {
                planResult
            }
            profileResult
                .onSuccess {
                    transientState.value = StudentProfileTransientState(
                        message = "Local profile and plans were deleted.",
                    )
                }
                .onFailure { showFailure("Porta could not clear all local profile data.") }
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
            fact.key to ProfileFactUiState(
                key = fact.key,
                label = fact.key.label(),
                value = fact.value.profileDisplayValue(fact.key),
                provenanceLabel = fact.provenance.label(),
                actionabilityLabel = fact.actionability.label(),
                evidenceLabel = fact.evidence?.label,
                dependentDoorTitles = opportunities
                    .filter { opportunity -> opportunity.eligibilityRule.uses(fact.key) }
                    .map(Opportunity::title),
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
        return StudentProfileUiState(
            isLoading = false,
            isWorking = transient.isWorking,
            hasRealProfile = facts.isNotEmpty(),
            factCount = facts.size,
            sections = sections,
            isSampleCatalog = isSampleCatalog,
            expandedFactKey = transient.expandedFactKey,
            factPendingDelete = transient.factPendingDelete,
            isClearConfirmationVisible = transient.isClearConfirmationVisible,
            message = transient.message,
        )
    }

    private fun RuleNode.uses(key: FactKey): Boolean = when (this) {
        is RuleNode.All -> children.any { it.uses(key) }
        is RuleNode.Any -> children.any { it.uses(key) }
        is RuleNode.Atomic -> factKey == key
        is RuleNode.ManualCheck -> false
    }

    private fun FactValue.profileDisplayValue(key: FactKey): String = when {
        key == FactKey.GPA && this is FactValue.NumberValue -> {
            val scale = unit.removeSuffix(" scale").ifBlank { "4.0" }
            "${amount.compact()} / $scale"
        }

        this is FactValue.NumberValue -> "${amount.compact()} $unit".trim()

        else -> displayValue()
    }

    private fun Double.compact(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

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
        val isClearConfirmationVisible: Boolean = false,
        val message: String? = null,
    )

    private data class ProfileSectionDefinition(
        val title: String,
        val description: String,
        val keys: List<FactKey>,
    )

    companion object {
        private val PROFILE_SECTIONS = listOf(
            ProfileSectionDefinition(
                title = "Academic",
                description = "Study status and measurable academic facts.",
                keys = listOf(
                    FactKey.STUDY_LEVEL,
                    FactKey.FIELD_OF_STUDY,
                    FactKey.GPA,
                    FactKey.IELTS_SCORE,
                    FactKey.ENROLLED,
                    FactKey.DEGREE_COMPLETED,
                ),
            ),
            ProfileSectionDefinition(
                title = "Location and identity context",
                description = "Context can explain a rule, but Porta never recommends changing it.",
                keys = listOf(FactKey.NATIONALITY, FactKey.RESIDENCY, FactKey.AGE),
            ),
            ProfileSectionDefinition(
                title = "Experience and availability",
                description = "Experience, projects and time commitments.",
                keys = listOf(
                    FactKey.EXPERIENCE_MONTHS,
                    FactKey.COMMUNITY_PROJECT,
                    FactKey.LEADERSHIP,
                    FactKey.AVAILABLE_FULL_PERIOD,
                ),
            ),
        )
    }
}
