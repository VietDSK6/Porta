package io.github.vietdsk6.porta.presentation.screens.profilesetup

data class ProfileSetupUiState(
    val studyLevel: StudyLevelOption? = null,
    val fieldOfStudy: String = "",
    val nationality: String = "",
    val gpa: String = "",
    val ielts: String = "",
    val age: String = "",
    val experienceMonths: String = "",
    val isEnrolled: Boolean = true,
    val isDegreeCompleted: Boolean? = null,
    val isAvailableFullPeriod: Boolean? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
) {
    val gpaValue: Double?
        get() = gpa.toDoubleOrNull()

    val ieltsValue: Double?
        get() = ielts.takeIf(String::isNotBlank)?.toDoubleOrNull()

    val ageValue: Double?
        get() = age.takeIf(String::isNotBlank)?.toDoubleOrNull()

    val experienceMonthsValue: Double?
        get() = experienceMonths.takeIf(String::isNotBlank)?.toDoubleOrNull()

    val gpaError: String?
        get() = when {
            gpa.isBlank() -> null
            gpaValue == null -> "Use a number, for example 3.6"
            gpaValue?.let { it !in 0.0..4.0 } == true -> "Use the 4.0 scale"
            else -> null
        }

    val ieltsError: String?
        get() = when {
            ielts.isBlank() -> null
            ieltsValue == null -> "Use a number, for example 6.5"
            ieltsValue?.let { it !in 0.0..9.0 } == true -> "IELTS bands range from 0 to 9"
            else -> null
        }

    val ageError: String?
        get() = when {
            age.isBlank() -> null
            ageValue == null -> "Use a whole number"
            ageValue?.let { it !in 1.0..120.0 || it % 1.0 != 0.0 } == true -> "Use an age from 1 to 120"
            else -> null
        }

    val experienceMonthsError: String?
        get() = when {
            experienceMonths.isBlank() -> null

            experienceMonthsValue == null -> "Use a whole number"

            experienceMonthsValue?.let { it !in 0.0..600.0 || it % 1.0 != 0.0 } == true -> {
                "Use whole months from 0 to 600"
            }

            else -> null
        }

    val canSave: Boolean
        get() = studyLevel != null &&
            fieldOfStudy.isNotBlank() &&
            nationality.isNotBlank() &&
            gpaValue?.let { it in 0.0..4.0 } == true &&
            ieltsError == null &&
            ageError == null &&
            experienceMonthsError == null &&
            !isSaving
}

enum class StudyLevelOption(
    val storedValue: String,
    val label: String,
) {
    UNDERGRADUATE("undergraduate", "Undergrad"),
    MASTERS("masters", "Master's"),
    DOCTORAL("doctoral", "Doctoral"),
}

sealed interface ProfileSetupUiEvent {
    data class StudyLevelChanged(val value: StudyLevelOption) : ProfileSetupUiEvent

    data class FieldOfStudyChanged(val value: String) : ProfileSetupUiEvent

    data class NationalityChanged(val value: String) : ProfileSetupUiEvent

    data class GpaChanged(val value: String) : ProfileSetupUiEvent

    data class IeltsChanged(val value: String) : ProfileSetupUiEvent

    data class AgeChanged(val value: String) : ProfileSetupUiEvent

    data class ExperienceMonthsChanged(val value: String) : ProfileSetupUiEvent

    data class EnrolledChanged(val value: Boolean) : ProfileSetupUiEvent

    data class DegreeCompletedChanged(val value: Boolean?) : ProfileSetupUiEvent

    data class AvailableFullPeriodChanged(val value: Boolean?) : ProfileSetupUiEvent

    data object Save : ProfileSetupUiEvent

    data object SaveHandled : ProfileSetupUiEvent
}
