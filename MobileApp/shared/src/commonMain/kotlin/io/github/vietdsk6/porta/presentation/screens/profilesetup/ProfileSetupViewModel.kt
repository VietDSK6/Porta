package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileSetupViewModel(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getProfile().onSuccess { profile ->
                if (profile.facts.isNotEmpty()) {
                    val facts = profile.facts
                    val studyLevel = (facts[FactKey.STUDY_LEVEL]?.value as? FactValue.EnumValue)?.value
                    _uiState.value = _uiState.value.copy(
                        studyLevel = StudyLevelOption.entries.firstOrNull {
                            it.storedValue.equals(studyLevel, ignoreCase = true)
                        },
                        fieldOfStudy = (facts[FactKey.FIELD_OF_STUDY]?.value as? FactValue.TextValue)?.value.orEmpty(),
                        nationality = (facts[FactKey.NATIONALITY]?.value as? FactValue.EnumValue)?.value.orEmpty(),
                        residency = (facts[FactKey.RESIDENCY]?.value as? FactValue.EnumValue)?.value.orEmpty(),
                        gpa = (facts[FactKey.GPA]?.value as? FactValue.NumberValue)?.amount?.toInputText().orEmpty(),
                        gpaScale = (facts[FactKey.GPA]?.value as? FactValue.NumberValue)
                            ?.unit
                            ?.let(::gpaScaleFromUnit)
                            ?: GpaScaleOption.FOUR,
                        ielts = (facts[FactKey.IELTS_SCORE]?.value as? FactValue.NumberValue)
                            ?.amount
                            ?.toInputText()
                            .orEmpty(),
                        age = (facts[FactKey.AGE]?.value as? FactValue.NumberValue)
                            ?.amount
                            ?.toInputText()
                            .orEmpty(),
                        experienceMonths = (facts[FactKey.EXPERIENCE_MONTHS]?.value as? FactValue.NumberValue)
                            ?.amount
                            ?.toInputText()
                            .orEmpty(),
                        isEnrolled = (facts[FactKey.ENROLLED]?.value as? FactValue.BooleanValue)?.value,
                        isDegreeCompleted = (facts[FactKey.DEGREE_COMPLETED]?.value as? FactValue.BooleanValue)?.value,
                        communityProjectTitle = facts.growthRecord(FactKey.COMMUNITY_PROJECT).primary,
                        communityProjectSummary = facts.growthRecord(FactKey.COMMUNITY_PROJECT).secondary,
                        communityProjectEvidenceReference = facts[FactKey.COMMUNITY_PROJECT]
                            ?.evidence
                            ?.reference
                            .orEmpty(),
                        leadershipRole = facts.growthRecord(FactKey.LEADERSHIP).primary,
                        leadershipTitle = facts.growthRecord(FactKey.LEADERSHIP).secondary,
                        leadershipEvidenceReference = facts[FactKey.LEADERSHIP]
                            ?.evidence
                            ?.reference
                            .orEmpty(),
                    )
                }
            }
        }
    }

    fun onUiEvent(event: ProfileSetupUiEvent) {
        when (event) {
            is ProfileSetupUiEvent.StudyLevelChanged -> update { copy(studyLevel = event.value) }

            is ProfileSetupUiEvent.FieldOfStudyChanged -> update { copy(fieldOfStudy = event.value) }

            is ProfileSetupUiEvent.NationalityChanged -> update { copy(nationality = event.value) }

            is ProfileSetupUiEvent.ResidencyChanged -> update { copy(residency = event.value) }

            is ProfileSetupUiEvent.GpaChanged -> update { copy(gpa = event.value.filterNumeric()) }

            is ProfileSetupUiEvent.GpaScaleChanged -> update { copy(gpaScale = event.value) }

            is ProfileSetupUiEvent.IeltsChanged -> update { copy(ielts = event.value.filterNumeric()) }

            is ProfileSetupUiEvent.AgeChanged -> update { copy(age = event.value.filterNumeric()) }

            is ProfileSetupUiEvent.ExperienceMonthsChanged -> {
                update { copy(experienceMonths = event.value.filterNumeric()) }
            }

            is ProfileSetupUiEvent.EnrolledChanged -> update { copy(isEnrolled = event.value) }

            is ProfileSetupUiEvent.DegreeCompletedChanged -> update { copy(isDegreeCompleted = event.value) }

            is ProfileSetupUiEvent.CommunityProjectTitleChanged -> {
                update { copy(communityProjectTitle = event.value) }
            }

            is ProfileSetupUiEvent.CommunityProjectSummaryChanged -> {
                update { copy(communityProjectSummary = event.value) }
            }

            is ProfileSetupUiEvent.CommunityProjectEvidenceChanged -> {
                update { copy(communityProjectEvidenceReference = event.value) }
            }

            is ProfileSetupUiEvent.LeadershipRoleChanged -> update { copy(leadershipRole = event.value) }

            is ProfileSetupUiEvent.LeadershipTitleChanged -> update { copy(leadershipTitle = event.value) }

            is ProfileSetupUiEvent.LeadershipEvidenceChanged -> {
                update { copy(leadershipEvidenceReference = event.value) }
            }

            ProfileSetupUiEvent.Save -> save()

            ProfileSetupUiEvent.SaveHandled -> update { copy(isSaved = false) }
        }
    }

    private fun update(transform: ProfileSetupUiState.() -> ProfileSetupUiState) {
        _uiState.update { state -> state.transform().copy(error = null) }
    }

    private fun save() {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            var result = profileRepository.saveFacts(state.toFacts())
            if (result.isSuccess) {
                state.omittedOptionalKeys().forEach { key ->
                    if (result.isSuccess) result = profileRepository.deleteFact(key)
                }
            }
            result
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Porta could not save this profile locally. Please try again.",
                        )
                    }
                }
        }
    }

    private fun ProfileSetupUiState.toFacts(): List<ProfileFact> {
        val timestamp = nowEpochMillis()
        val values = buildList<FactDraft> {
            add(FactDraft(FactKey.STUDY_LEVEL, FactValue.EnumValue(checkNotNull(studyLevel).storedValue)))
            add(FactDraft(FactKey.FIELD_OF_STUDY, FactValue.TextValue(fieldOfStudy.trim())))
            nationality.takeIf(String::isNotBlank)?.let {
                add(FactDraft(FactKey.NATIONALITY, FactValue.EnumValue(it.uppercase())))
            }
            residency.takeIf(String::isNotBlank)?.let {
                add(FactDraft(FactKey.RESIDENCY, FactValue.EnumValue(it.uppercase())))
            }
            gpaValue?.let {
                add(FactDraft(FactKey.GPA, FactValue.NumberValue(it, "${gpaScale.label} scale")))
            }
            isEnrolled?.let { add(FactDraft(FactKey.ENROLLED, FactValue.BooleanValue(it))) }
            ieltsValue?.let { add(FactDraft(FactKey.IELTS_SCORE, FactValue.NumberValue(it, "band"))) }
            ageValue?.let { add(FactDraft(FactKey.AGE, FactValue.NumberValue(it, "years"))) }
            experienceMonthsValue?.let {
                add(FactDraft(FactKey.EXPERIENCE_MONTHS, FactValue.NumberValue(it, "months")))
            }
            isDegreeCompleted?.let {
                add(FactDraft(FactKey.DEGREE_COMPLETED, FactValue.BooleanValue(it)))
            }
            growthDraft(
                key = FactKey.COMMUNITY_PROJECT,
                primary = communityProjectTitle,
                secondary = communityProjectSummary,
                evidenceLabel = "Community project evidence",
                evidenceReference = communityProjectEvidenceReference,
            )?.let(::add)
            growthDraft(
                key = FactKey.LEADERSHIP,
                primary = leadershipRole,
                secondary = leadershipTitle,
                evidenceLabel = "Leadership evidence",
                evidenceReference = leadershipEvidenceReference,
            )?.let(::add)
        }
        return values.map { draft ->
            ProfileFact(
                id = "${ProfileFact.DEFAULT_PROFILE_ID}:${draft.key.name.lowercase()}",
                key = draft.key,
                value = draft.value,
                provenance = draft.provenance,
                actionability = draft.key.actionability(),
                evidence = draft.evidence,
                updatedAtEpochMillis = timestamp,
            )
        }
    }

    private fun FactKey.actionability(): Actionability = when (this) {
        FactKey.GPA,
        FactKey.IELTS_SCORE,
        -> Actionability.CONTROLLABLE

        FactKey.STUDY_LEVEL,
        FactKey.ENROLLED,
        FactKey.DEGREE_COMPLETED,
        FactKey.EXPERIENCE_MONTHS,
        -> Actionability.TIME_DEPENDENT

        FactKey.AGE -> Actionability.IMMUTABLE

        FactKey.FIELD_OF_STUDY,
        FactKey.NATIONALITY,
        FactKey.RESIDENCY,
        -> Actionability.CONTEXT_ONLY

        FactKey.COMMUNITY_PROJECT,
        FactKey.LEADERSHIP,
        FactKey.AVAILABLE_FULL_PERIOD,
        -> Actionability.UNKNOWN
    }

    private fun ProfileSetupUiState.omittedOptionalKeys(): List<FactKey> = buildList {
        if (ieltsValue == null) add(FactKey.IELTS_SCORE)
        if (gpaValue == null) add(FactKey.GPA)
        if (ageValue == null) add(FactKey.AGE)
        if (experienceMonthsValue == null) add(FactKey.EXPERIENCE_MONTHS)
        if (isDegreeCompleted == null) add(FactKey.DEGREE_COMPLETED)
        if (isEnrolled == null) add(FactKey.ENROLLED)
        if (nationality.isBlank()) add(FactKey.NATIONALITY)
        if (residency.isBlank()) add(FactKey.RESIDENCY)
        if (communityProjectTitle.isBlank() && communityProjectSummary.isBlank()) {
            add(FactKey.COMMUNITY_PROJECT)
        }
        if (leadershipRole.isBlank() && leadershipTitle.isBlank()) add(FactKey.LEADERSHIP)
    }

    private fun String.filterNumeric(): String = filter { it.isDigit() || it == '.' }
        .let { value ->
            val firstDot = value.indexOf('.')
            if (firstDot == -1) value else value.take(firstDot + 1) + value.drop(firstDot + 1).replace(".", "")
        }

    private fun Double.toInputText(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

    private fun Map<FactKey, ProfileFact>.growthRecord(key: FactKey): GrowthEvidenceRecord {
        val text = (get(key)?.value as? FactValue.TextValue)?.value.orEmpty()
        return GrowthEvidenceEncoding.decode(text)
    }

    private fun growthDraft(
        key: FactKey,
        primary: String,
        secondary: String,
        evidenceLabel: String,
        evidenceReference: String,
    ): FactDraft? {
        if (primary.isBlank() && secondary.isBlank()) return null
        val evidence = evidenceReference.trim().takeIf(String::isNotBlank)?.let { reference ->
            EvidenceMetadata(label = evidenceLabel, reference = reference)
        }
        return FactDraft(
            key = key,
            value = FactValue.TextValue(GrowthEvidenceEncoding.encode(primary, secondary)),
            provenance = if (evidence == null) FactProvenance.SELF_REPORTED else FactProvenance.EVIDENCE_BACKED,
            evidence = evidence,
        )
    }

    private data class FactDraft(
        val key: FactKey,
        val value: FactValue,
        val provenance: FactProvenance = FactProvenance.SELF_REPORTED,
        val evidence: EvidenceMetadata? = null,
    )
}

private fun gpaScaleFromUnit(unit: String): GpaScaleOption = if (unit.startsWith("10")) GpaScaleOption.TEN else GpaScaleOption.FOUR
