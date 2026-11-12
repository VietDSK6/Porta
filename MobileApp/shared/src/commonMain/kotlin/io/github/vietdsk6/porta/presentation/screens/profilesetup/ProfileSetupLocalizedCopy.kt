package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Keeps validation decisions pure while rendering their visible copy in the selected language. */
@Composable
internal fun profileSetupMessage(message: String?, gpaScale: GpaScaleOption): String? = when (message) {
    null -> null
    "Use a number, for example ${gpaScale.example}" -> stringResource(Res.string.profile_setup_error_gpa_number, gpaScale.example)
    "Use the ${gpaScale.label} scale" -> stringResource(Res.string.profile_setup_error_gpa_scale, gpaScale.label)
    "Use a number, for example 6.5" -> stringResource(Res.string.profile_setup_error_ielts_number)
    "IELTS bands range from 0 to 9" -> stringResource(Res.string.profile_setup_error_ielts_range)
    "Use a whole or half band, for example 6.5" -> stringResource(Res.string.profile_setup_error_ielts_step)
    "Use a whole number" -> stringResource(Res.string.profile_setup_error_whole_number)
    "Use an age from 1 to 120" -> stringResource(Res.string.profile_setup_error_age_range)
    "Use whole months from 0 to 600" -> stringResource(Res.string.profile_setup_error_month_range)
    "Use YYYY-MM-DD, for example 2004-09-18" -> stringResource(Res.string.profile_setup_error_birth_format)
    "Use a year from 1900 to 2100" -> stringResource(Res.string.profile_setup_error_birth_year)
    "Date of birth cannot be in the future" -> stringResource(Res.string.profile_setup_error_birth_future)
    "Choose a country from the list" -> stringResource(Res.string.profile_setup_error_country)
    "Choose your current study level to continue." -> stringResource(Res.string.profile_setup_error_study_level)
    "Add your field of study to continue." -> stringResource(Res.string.profile_setup_error_field)
    "Check the GPA value before saving." -> stringResource(Res.string.profile_setup_error_check_gpa)
    "Check the IELTS value before saving." -> stringResource(Res.string.profile_setup_error_check_ielts)
    "Check the age value before saving." -> stringResource(Res.string.profile_setup_error_check_age)
    "Check the date of birth before saving." -> stringResource(Res.string.profile_setup_error_check_birth)
    "Choose a valid nationality before saving." -> stringResource(Res.string.profile_setup_error_check_nationality)
    "Choose a valid country of residence before saving." -> stringResource(Res.string.profile_setup_error_check_residency)
    "Check the experience duration before saving." -> stringResource(Res.string.profile_setup_error_check_experience)
    "Add a project title or summary for that evidence reference." -> stringResource(Res.string.profile_setup_error_project_evidence)
    "Add a leadership role or activity for that evidence reference." -> stringResource(Res.string.profile_setup_error_leadership_evidence)
    "Porta could not save this profile locally. Please try again." -> stringResource(Res.string.profile_setup_error_save)
    else -> message
}

@Composable
internal fun StudyLevelOption.localizedLabel(): String = when (this) {
    StudyLevelOption.UNDERGRADUATE -> stringResource(Res.string.profile_setup_level_undergraduate)
    StudyLevelOption.MASTERS -> stringResource(Res.string.profile_setup_level_masters)
    StudyLevelOption.DOCTORAL -> stringResource(Res.string.profile_setup_level_doctoral)
}
