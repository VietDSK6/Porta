package io.github.vietdsk6.porta.presentation.locale

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FactKey.toText(): String = stringResource(
    when (this) {
        FactKey.GPA -> Res.string.fact_key_gpa
        FactKey.STUDY_LEVEL -> Res.string.fact_key_study_level
        FactKey.FIELD_OF_STUDY -> Res.string.fact_key_field_of_study
        FactKey.NATIONALITY -> Res.string.fact_key_nationality
        FactKey.RESIDENCY -> Res.string.fact_key_residency
        FactKey.IELTS_SCORE -> Res.string.fact_key_ielts_score
        FactKey.EXPERIENCE_MONTHS -> Res.string.fact_key_experience_months
        FactKey.COMMUNITY_PROJECT -> Res.string.fact_key_community_project
        FactKey.LEADERSHIP -> Res.string.fact_key_leadership
        FactKey.ENROLLED -> Res.string.fact_key_enrolled
        FactKey.AGE -> Res.string.fact_key_age
        FactKey.DATE_OF_BIRTH -> Res.string.fact_key_date_of_birth
        FactKey.DEGREE_COMPLETED -> Res.string.fact_key_degree_completed
        FactKey.AVAILABLE_FULL_PERIOD -> Res.string.fact_key_available_full_period
    },
)
