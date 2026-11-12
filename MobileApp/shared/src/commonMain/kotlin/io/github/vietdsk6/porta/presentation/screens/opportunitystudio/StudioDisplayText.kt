package io.github.vietdsk6.porta.presentation.screens.opportunitystudio

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.StudioDeadlineKind
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.domain.model.StudioRootRuleKind
import io.github.vietdsk6.porta.generated.resources.Res
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
import io.github.vietdsk6.porta.generated.resources.studio_enum_actionability_context_only
import io.github.vietdsk6.porta.generated.resources.studio_enum_actionability_controllable
import io.github.vietdsk6.porta.generated.resources.studio_enum_actionability_immutable
import io.github.vietdsk6.porta.generated.resources.studio_enum_actionability_time_dependent
import io.github.vietdsk6.porta.generated.resources.studio_enum_actionability_unknown
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_academic_program
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_fellowship
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_internship
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_scholarship
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_student_contest
import io.github.vietdsk6.porta.generated.resources.studio_enum_deadline_known
import io.github.vietdsk6.porta.generated.resources.studio_enum_deadline_missing
import io.github.vietdsk6.porta.generated.resources.studio_enum_deadline_tba
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_contains_all
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_contains_any
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_date_after
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_date_before
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_equals
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_exists
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_greater_than_or_equal
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_in_set
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_less_than_or_equal
import io.github.vietdsk6.porta.generated.resources.studio_enum_operator_not_equals
import io.github.vietdsk6.porta.generated.resources.studio_enum_root_all
import io.github.vietdsk6.porta.generated.resources.studio_enum_root_any
import io.github.vietdsk6.porta.generated.resources.studio_enum_value_kind_boolean
import io.github.vietdsk6.porta.generated.resources.studio_enum_value_kind_date
import io.github.vietdsk6.porta.generated.resources.studio_enum_value_kind_duration
import io.github.vietdsk6.porta.generated.resources.studio_enum_value_kind_enum
import io.github.vietdsk6.porta.generated.resources.studio_enum_value_kind_enum_list
import io.github.vietdsk6.porta.generated.resources.studio_enum_value_kind_number
import io.github.vietdsk6.porta.generated.resources.studio_enum_value_kind_text
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FactKey.localizedStudioLabel(): String = stringResource(
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
internal fun OpportunityCategory.localizedStudioLabel(): String = stringResource(
    when (this) {
        OpportunityCategory.SCHOLARSHIP -> Res.string.studio_enum_category_scholarship
        OpportunityCategory.INTERNSHIP -> Res.string.studio_enum_category_internship
        OpportunityCategory.ACADEMIC_PROGRAM -> Res.string.studio_enum_category_academic_program
        OpportunityCategory.STUDENT_CONTEST -> Res.string.studio_enum_category_student_contest
        OpportunityCategory.FELLOWSHIP -> Res.string.studio_enum_category_fellowship
    },
)

@Composable
internal fun StudioDeadlineKind.localizedStudioLabel(): String = stringResource(
    when (this) {
        StudioDeadlineKind.KNOWN -> Res.string.studio_enum_deadline_known
        StudioDeadlineKind.TBA -> Res.string.studio_enum_deadline_tba
        StudioDeadlineKind.MISSING -> Res.string.studio_enum_deadline_missing
    },
)

@Composable
internal fun StudioRootRuleKind.localizedStudioLabel(): String = stringResource(
    when (this) {
        StudioRootRuleKind.ALL -> Res.string.studio_enum_root_all
        StudioRootRuleKind.ANY -> Res.string.studio_enum_root_any
    },
)

@Composable
internal fun Operator.localizedStudioLabel(): String = stringResource(
    when (this) {
        Operator.EXISTS -> Res.string.studio_enum_operator_exists
        Operator.EQUALS -> Res.string.studio_enum_operator_equals
        Operator.NOT_EQUALS -> Res.string.studio_enum_operator_not_equals
        Operator.GREATER_THAN_OR_EQUAL -> Res.string.studio_enum_operator_greater_than_or_equal
        Operator.LESS_THAN_OR_EQUAL -> Res.string.studio_enum_operator_less_than_or_equal
        Operator.IN_SET -> Res.string.studio_enum_operator_in_set
        Operator.CONTAINS_ANY -> Res.string.studio_enum_operator_contains_any
        Operator.CONTAINS_ALL -> Res.string.studio_enum_operator_contains_all
        Operator.DATE_BEFORE -> Res.string.studio_enum_operator_date_before
        Operator.DATE_AFTER -> Res.string.studio_enum_operator_date_after
    },
)

@Composable
internal fun Actionability.localizedStudioLabel(): String = stringResource(
    when (this) {
        Actionability.CONTROLLABLE -> Res.string.studio_enum_actionability_controllable
        Actionability.TIME_DEPENDENT -> Res.string.studio_enum_actionability_time_dependent
        Actionability.CONTEXT_ONLY -> Res.string.studio_enum_actionability_context_only
        Actionability.IMMUTABLE -> Res.string.studio_enum_actionability_immutable
        Actionability.UNKNOWN -> Res.string.studio_enum_actionability_unknown
    },
)

@Composable
internal fun StudioFactValueKind.localizedStudioLabel(): String = stringResource(
    when (this) {
        StudioFactValueKind.BOOLEAN -> Res.string.studio_enum_value_kind_boolean
        StudioFactValueKind.NUMBER -> Res.string.studio_enum_value_kind_number
        StudioFactValueKind.ENUM -> Res.string.studio_enum_value_kind_enum
        StudioFactValueKind.ENUM_LIST -> Res.string.studio_enum_value_kind_enum_list
        StudioFactValueKind.TEXT -> Res.string.studio_enum_value_kind_text
        StudioFactValueKind.DATE -> Res.string.studio_enum_value_kind_date
        StudioFactValueKind.DURATION -> Res.string.studio_enum_value_kind_duration
    },
)
