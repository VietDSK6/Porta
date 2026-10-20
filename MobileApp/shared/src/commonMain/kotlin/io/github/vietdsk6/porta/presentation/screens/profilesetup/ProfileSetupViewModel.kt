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
                        isEnrolled = (facts[FactKey.ENROLLED]?.value as? FactValue.BooleanValue)?.value ?: true,
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
            is ProfileSetupUiEvent.EnrolledChanged -> update { copy(isEnrolled = event.value) }
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
            val saveResult = profileRepository.saveFacts(state.toFacts())
            val result = if (saveResult.isSuccess && state.ieltsValue == null) {
                profileRepository.deleteFact(FactKey.IELTS_SCORE)
            } else {
                saveResult
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
        -> Actionability.TIME_DEPENDENT

        FactKey.FIELD_OF_STUDY,
        FactKey.NATIONALITY,
        -> Actionability.CONTEXT_ONLY

        else -> Actionability.UNKNOWN
    }

    private fun String.filterNumeric(): String = filter { it.isDigit() || it == '.' }
        .let { value ->
            val firstDot = value.indexOf('.')
            if (firstDot == -1) value else value.take(firstDot + 1) + value.drop(firstDot + 1).replace(".", "")
        }

    private fun Double.toInputText(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()
}
