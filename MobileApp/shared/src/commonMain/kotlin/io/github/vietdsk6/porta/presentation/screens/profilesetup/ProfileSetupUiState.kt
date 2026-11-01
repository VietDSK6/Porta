package io.github.vietdsk6.porta.presentation.screens.profilesetup

import kotlinx.datetime.LocalDate

data class ProfileSetupUiState(
    val studyLevel: StudyLevelOption? = null,
    val fieldOfStudy: String = "",
    val nationality: String = "",
    val residency: String = "",
    val gpa: String = "",
    val gpaScale: GpaScaleOption = GpaScaleOption.FOUR,
    val ielts: String = "",
    val age: String = "",
    val dateOfBirth: String = "",
    val validationDate: LocalDate? = null,
    val experienceMonths: String = "",
    val isEnrolled: Boolean? = null,
    val isDegreeCompleted: Boolean? = null,
    val communityProjectTitle: String = "",
    val communityProjectSummary: String = "",
    val communityProjectEvidenceReference: String = "",
    val leadershipRole: String = "",
    val leadershipTitle: String = "",
    val leadershipEvidenceReference: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
) {
    val gpaValue: Double?
        get() = gpa.takeIf(String::isNotBlank)?.toDoubleOrNull()

    val ieltsValue: Double?
        get() = ielts.takeIf(String::isNotBlank)?.toDoubleOrNull()

    val ageValue: Double?
        get() = age.takeIf(String::isNotBlank)?.toDoubleOrNull()

    val dateOfBirthValue: LocalDate?
        get() = dateOfBirth.takeIf(String::isNotBlank)?.let { value ->
            runCatching { LocalDate.parse(value) }.getOrNull()
        }

    val experienceMonthsValue: Double?
        get() = experienceMonths.takeIf(String::isNotBlank)?.toDoubleOrNull()

    val gpaError: String?
        get() = when {
            gpa.isBlank() -> null
            gpaValue == null -> "Use a number, for example ${gpaScale.example}"
            gpaValue?.let { it !in 0.0..gpaScale.maximum } == true -> "Use the ${gpaScale.label} scale"
            else -> null
        }

    val ieltsError: String?
        get() = when {
            ielts.isBlank() -> null
            ieltsValue == null -> "Use a number, for example 6.5"
            ieltsValue?.let { it !in 0.0..9.0 } == true -> "IELTS bands range from 0 to 9"
            ieltsValue?.let { it * 2 % 1.0 != 0.0 } == true -> "Use a whole or half band, for example 6.5"
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

    val dateOfBirthError: String?
        get() = when {
            dateOfBirth.isBlank() -> null

            dateOfBirthValue == null -> "Use YYYY-MM-DD, for example 2004-09-18"

            dateOfBirthValue?.year?.let { it !in 1900..2100 } == true -> "Use a year from 1900 to 2100"

            validationDate != null && dateOfBirthValue?.let { it > validationDate } == true -> {
                "Date of birth cannot be in the future"
            }

            else -> null
        }

    val nationalityError: String?
        get() = nationality.takeIf(String::isNotBlank)
            ?.takeIf { countryForStoredValue(it) == null }
            ?.let { "Choose a country from the list" }

    val residencyError: String?
        get() = residency.takeIf(String::isNotBlank)
            ?.takeIf { countryForStoredValue(it) == null }
            ?.let { "Choose a country from the list" }

    val canSave: Boolean
        get() = saveDisabledReason == null && !isSaving

    val saveDisabledReason: String?
        get() = when {
            studyLevel == null -> "Choose your current study level to continue."

            fieldOfStudy.isBlank() -> "Add your field of study to continue."

            gpaError != null -> "Check the GPA value before saving."

            ieltsError != null -> "Check the IELTS value before saving."

            ageError != null -> "Check the age value before saving."

            dateOfBirthError != null -> "Check the date of birth before saving."

            nationalityError != null -> "Choose a valid nationality before saving."

            residencyError != null -> "Choose a valid country of residence before saving."

            experienceMonthsError != null -> "Check the experience duration before saving."

            communityProjectEvidenceReference.isNotBlank() &&
                communityProjectTitle.isBlank() &&
                communityProjectSummary.isBlank() -> "Add a project title or summary for that evidence reference."

            leadershipEvidenceReference.isNotBlank() &&
                leadershipRole.isBlank() &&
                leadershipTitle.isBlank() -> "Add a leadership role or activity for that evidence reference."

            else -> null
        }

    val academicProvidedCount: Int
        get() = listOf(
            gpa.isNotBlank(),
            ielts.isNotBlank(),
            isEnrolled != null,
            isDegreeCompleted != null,
        ).count { it }

    val experienceProvidedCount: Int
        get() = listOf(
            experienceMonths.isNotBlank(),
            communityProjectTitle.isNotBlank() || communityProjectSummary.isNotBlank(),
            leadershipRole.isNotBlank() || leadershipTitle.isNotBlank(),
        ).count { it }

    val contextProvidedCount: Int
        get() = listOf(
            nationality.isNotBlank(),
            residency.isNotBlank(),
            age.isNotBlank(),
            dateOfBirth.isNotBlank(),
        ).count { it }
}

enum class StudyLevelOption(
    val storedValue: String,
    val label: String,
) {
    UNDERGRADUATE("undergraduate", "Undergrad"),
    MASTERS("masters", "Master's"),
    DOCTORAL("doctoral", "Doctoral"),
}

enum class GpaScaleOption(
    val label: String,
    val maximum: Double,
    val example: String,
) {
    FOUR("4.0", 4.0, "3.6"),
    TEN("10.0", 10.0, "8.5"),
}

sealed interface ProfileSetupUiEvent {
    data class StudyLevelChanged(val value: StudyLevelOption) : ProfileSetupUiEvent

    data class FieldOfStudyChanged(val value: String) : ProfileSetupUiEvent

    data class NationalityChanged(val value: String) : ProfileSetupUiEvent

    data class ResidencyChanged(val value: String) : ProfileSetupUiEvent

    data class GpaChanged(val value: String) : ProfileSetupUiEvent

    data class GpaScaleChanged(val value: GpaScaleOption) : ProfileSetupUiEvent

    data class IeltsChanged(val value: String) : ProfileSetupUiEvent

    data class AgeChanged(val value: String) : ProfileSetupUiEvent

    data class DateOfBirthChanged(val value: String) : ProfileSetupUiEvent

    data class ExperienceMonthsChanged(val value: String) : ProfileSetupUiEvent

    data class EnrolledChanged(val value: Boolean?) : ProfileSetupUiEvent

    data class DegreeCompletedChanged(val value: Boolean?) : ProfileSetupUiEvent

    data class CommunityProjectTitleChanged(val value: String) : ProfileSetupUiEvent

    data class CommunityProjectSummaryChanged(val value: String) : ProfileSetupUiEvent

    data class CommunityProjectEvidenceChanged(val value: String) : ProfileSetupUiEvent

    data class LeadershipRoleChanged(val value: String) : ProfileSetupUiEvent

    data class LeadershipTitleChanged(val value: String) : ProfileSetupUiEvent

    data class LeadershipEvidenceChanged(val value: String) : ProfileSetupUiEvent

    data object Save : ProfileSetupUiEvent

    data object SaveHandled : ProfileSetupUiEvent
}
