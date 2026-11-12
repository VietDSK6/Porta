package io.github.vietdsk6.porta.presentation.screens.studentprofile

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_actionability_context_only
import io.github.vietdsk6.porta.generated.resources.profile_actionability_controllable
import io.github.vietdsk6.porta.generated.resources.profile_actionability_immutable
import io.github.vietdsk6.porta.generated.resources.profile_actionability_time_dependent
import io.github.vietdsk6.porta.generated.resources.profile_actionability_unknown
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_age
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_available_full_period
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_community_project
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_date_of_birth
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_degree_completed
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_enrolled
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_experience_months
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_field_of_study
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_gpa
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_ielts_score
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_leadership
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_nationality
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_residency
import io.github.vietdsk6.porta.generated.resources.profile_fact_key_study_level
import io.github.vietdsk6.porta.generated.resources.profile_provenance_evidence_backed
import io.github.vietdsk6.porta.generated.resources.profile_provenance_self_reported
import io.github.vietdsk6.porta.generated.resources.profile_section_academic
import io.github.vietdsk6.porta.generated.resources.profile_section_context
import io.github.vietdsk6.porta.generated.resources.profile_section_essentials
import io.github.vietdsk6.porta.generated.resources.profile_section_experience
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FactKey.localizedLabel(): String = stringResource(
    when (this) {
        FactKey.GPA -> Res.string.profile_fact_key_gpa
        FactKey.STUDY_LEVEL -> Res.string.profile_fact_key_study_level
        FactKey.FIELD_OF_STUDY -> Res.string.profile_fact_key_field_of_study
        FactKey.NATIONALITY -> Res.string.profile_fact_key_nationality
        FactKey.RESIDENCY -> Res.string.profile_fact_key_residency
        FactKey.IELTS_SCORE -> Res.string.profile_fact_key_ielts_score
        FactKey.EXPERIENCE_MONTHS -> Res.string.profile_fact_key_experience_months
        FactKey.COMMUNITY_PROJECT -> Res.string.profile_fact_key_community_project
        FactKey.LEADERSHIP -> Res.string.profile_fact_key_leadership
        FactKey.ENROLLED -> Res.string.profile_fact_key_enrolled
        FactKey.AGE -> Res.string.profile_fact_key_age
        FactKey.DATE_OF_BIRTH -> Res.string.profile_fact_key_date_of_birth
        FactKey.DEGREE_COMPLETED -> Res.string.profile_fact_key_degree_completed
        FactKey.AVAILABLE_FULL_PERIOD -> Res.string.profile_fact_key_available_full_period
    },
)

@Composable
internal fun String.localizedProfileProvenance(): String = when (this) {
    "Self-reported" -> stringResource(Res.string.profile_provenance_self_reported)
    "Evidence-backed" -> stringResource(Res.string.profile_provenance_evidence_backed)
    else -> this
}

@Composable
internal fun String.localizedProfileActionability(): String = when (this) {
    "Can change" -> stringResource(Res.string.profile_actionability_controllable)
    "Changes with time" -> stringResource(Res.string.profile_actionability_time_dependent)
    "Context only" -> stringResource(Res.string.profile_actionability_context_only)
    "Not an action target" -> stringResource(Res.string.profile_actionability_immutable)
    "Actionability unknown" -> stringResource(Res.string.profile_actionability_unknown)
    else -> this
}

@Composable
internal fun String.localizedProfileSection(): String = when (this) {
    "Essentials" -> stringResource(Res.string.profile_section_essentials)
    "Academic record" -> stringResource(Res.string.profile_section_academic)
    "Experience and evidence" -> stringResource(Res.string.profile_section_experience)
    "Context" -> stringResource(Res.string.profile_section_context)
    else -> this
}
