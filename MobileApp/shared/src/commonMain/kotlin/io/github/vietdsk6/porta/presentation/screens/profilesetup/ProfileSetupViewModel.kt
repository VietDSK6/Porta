package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
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
                        gpa = (facts[FactKey.GPA]?.value as? FactValue.NumberValue)?.amount?.toInputText().orEmpty(),
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
                        isEnrolled = (facts[FactKey.ENROLLED]?.value as? FactValue.BooleanValue)?.value ?: true,
                        isDegreeCompleted = (facts[FactKey.DEGREE_COMPLETED]?.value as? FactValue.BooleanValue)?.value,
                        isAvailableFullPeriod = facts[FactKey.AVAILABLE_FULL_PERIOD]
                            ?.value
                            .let { it as? FactValue.BooleanValue }
                            ?.value,
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

            is ProfileSetupUiEvent.GpaChanged -> update { copy(gpa = event.value.filterNumeric()) }

            is ProfileSetupUiEvent.IeltsChanged -> update { copy(ielts = event.value.filterNumeric()) }

            is ProfileSetupUiEvent.AgeChanged -> update { copy(age = event.value.filterNumeric()) }

            is ProfileSetupUiEvent.ExperienceMonthsChanged -> {
                update { copy(experienceMonths = event.value.filterNumeric()) }
            }

            is ProfileSetupUiEvent.EnrolledChanged -> update { copy(isEnrolled = event.value) }

            is ProfileSetupUiEvent.DegreeCompletedChanged -> update { copy(isDegreeCompleted = event.value) }

            is ProfileSetupUiEvent.AvailableFullPeriodChanged -> {
                update { copy(isAvailableFullPeriod = event.value) }
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
        val values = buildList {
            add(FactKey.STUDY_LEVEL to FactValue.EnumValue(checkNotNull(studyLevel).storedValue))
            add(FactKey.FIELD_OF_STUDY to FactValue.TextValue(fieldOfStudy.trim()))
            add(FactKey.NATIONALITY to FactValue.EnumValue(nationality.trim().uppercase()))
            add(FactKey.GPA to FactValue.NumberValue(checkNotNull(gpaValue), "4.0 scale"))
            add(FactKey.ENROLLED to FactValue.BooleanValue(isEnrolled))
            ieltsValue?.let { add(FactKey.IELTS_SCORE to FactValue.NumberValue(it, "band")) }
            ageValue?.let { add(FactKey.AGE to FactValue.NumberValue(it, "years")) }
            experienceMonthsValue?.let {
                add(FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(it, "months"))
            }
            isDegreeCompleted?.let { add(FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(it)) }
            isAvailableFullPeriod?.let {
                add(FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(it))
            }
        }
        return values.map { (key, value) ->
            ProfileFact(
                id = "${ProfileFact.DEFAULT_PROFILE_ID}:${key.name.lowercase()}",
                key = key,
                value = value,
                actionability = key.actionability(),
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

        FactKey.AVAILABLE_FULL_PERIOD -> Actionability.CONTROLLABLE

        FactKey.AGE -> Actionability.IMMUTABLE

        FactKey.FIELD_OF_STUDY,
        FactKey.NATIONALITY,
        -> Actionability.CONTEXT_ONLY

        else -> Actionability.UNKNOWN
    }

    private fun ProfileSetupUiState.omittedOptionalKeys(): List<FactKey> = buildList {
        if (ieltsValue == null) add(FactKey.IELTS_SCORE)
        if (ageValue == null) add(FactKey.AGE)
        if (experienceMonthsValue == null) add(FactKey.EXPERIENCE_MONTHS)
        if (isDegreeCompleted == null) add(FactKey.DEGREE_COMPLETED)
        if (isAvailableFullPeriod == null) add(FactKey.AVAILABLE_FULL_PERIOD)
    }

    private fun String.filterNumeric(): String = filter { it.isDigit() || it == '.' }
        .let { value ->
            val firstDot = value.indexOf('.')
            if (firstDot == -1) value else value.take(firstDot + 1) + value.drop(firstDot + 1).replace(".", "")
        }

    private fun Double.toInputText(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()
}
