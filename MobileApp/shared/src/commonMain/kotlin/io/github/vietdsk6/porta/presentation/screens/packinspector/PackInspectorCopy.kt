package io.github.vietdsk6.porta.presentation.screens.packinspector

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.domain.model.DEFAULT_SOURCE_REVIEW_WINDOW_DAYS
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.SourceFreshness
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.pack_meta_all_current
import io.github.vietdsk6.porta.generated.resources.pack_meta_checked
import io.github.vietdsk6.porta.generated.resources.pack_meta_imported
import io.github.vietdsk6.porta.generated.resources.pack_meta_new_version
import io.github.vietdsk6.porta.generated.resources.pack_meta_no_review_dates
import io.github.vietdsk6.porta.generated.resources.pack_meta_records
import io.github.vietdsk6.porta.generated.resources.pack_meta_records_reviewed_on
import io.github.vietdsk6.porta.generated.resources.pack_meta_records_reviewed_range
import io.github.vietdsk6.porta.generated.resources.pack_meta_review_due
import io.github.vietdsk6.porta.generated.resources.pack_meta_schema
import io.github.vietdsk6.porta.generated.resources.pack_meta_version
import io.github.vietdsk6.porta.generated.resources.pack_meta_version_transition
import io.github.vietdsk6.porta.generated.resources.pack_notice_activated
import io.github.vietdsk6.porta.generated.resources.pack_notice_export_failed
import io.github.vietdsk6.porta.generated.resources.pack_notice_exported_diagnostics
import io.github.vietdsk6.porta.generated.resources.pack_notice_exported_pack
import io.github.vietdsk6.porta.generated.resources.pack_notice_import_read_failed
import io.github.vietdsk6.porta.generated.resources.pack_notice_import_rejected
import io.github.vietdsk6.porta.generated.resources.pack_notice_load_failed
import io.github.vietdsk6.porta.generated.resources.pack_notice_matches
import io.github.vietdsk6.porta.generated.resources.pack_notice_save_failed
import io.github.vietdsk6.porta.generated.resources.pack_notice_stale
import io.github.vietdsk6.porta.generated.resources.pack_operator_after
import io.github.vietdsk6.porta.generated.resources.pack_operator_before
import io.github.vietdsk6.porta.generated.resources.pack_operator_contains_all
import io.github.vietdsk6.porta.generated.resources.pack_operator_contains_any
import io.github.vietdsk6.porta.generated.resources.pack_operator_equals
import io.github.vietdsk6.porta.generated.resources.pack_operator_exists
import io.github.vietdsk6.porta.generated.resources.pack_operator_gte
import io.github.vietdsk6.porta.generated.resources.pack_operator_in_set
import io.github.vietdsk6.porta.generated.resources.pack_operator_lte
import io.github.vietdsk6.porta.generated.resources.pack_operator_not_equals
import io.github.vietdsk6.porta.generated.resources.pack_record_review_current
import io.github.vietdsk6.porta.generated.resources.pack_record_review_due
import io.github.vietdsk6.porta.generated.resources.pack_record_review_future
import io.github.vietdsk6.porta.generated.resources.pack_record_review_predates
import io.github.vietdsk6.porta.generated.resources.pack_rule_all
import io.github.vietdsk6.porta.generated.resources.pack_rule_all_summary
import io.github.vietdsk6.porta.generated.resources.pack_rule_any
import io.github.vietdsk6.porta.generated.resources.pack_rule_any_summary
import io.github.vietdsk6.porta.generated.resources.pack_rule_atomic
import io.github.vietdsk6.porta.generated.resources.pack_rule_manual
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
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_academic_program
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_fellowship
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_internship
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_scholarship
import io.github.vietdsk6.porta.generated.resources.studio_enum_category_student_contest
import io.github.vietdsk6.porta.presentation.locale.asLocalizedDate
import io.github.vietdsk6.porta.presentation.locale.formatLocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PackInspectorNotice.toText(): String = when (this) {
    is PackInspectorNotice.MatchesActive -> stringResource(Res.string.pack_notice_matches, fileName)

    is PackInspectorNotice.Activated -> {
        val activated = stringResource(Res.string.pack_notice_activated, fileName)
        val stale = if (staleAssertionCount > 0) {
            " " + pluralStringResource(Res.plurals.pack_notice_stale, staleAssertionCount, staleAssertionCount)
        } else {
            ""
        }
        activated + stale
    }

    PackInspectorNotice.ImportReadFailed -> stringResource(Res.string.pack_notice_import_read_failed)

    PackInspectorNotice.ImportRejected -> stringResource(Res.string.pack_notice_import_rejected)

    PackInspectorNotice.ExportedPack -> stringResource(Res.string.pack_notice_exported_pack)

    PackInspectorNotice.ExportedDiagnostics -> stringResource(Res.string.pack_notice_exported_diagnostics)

    PackInspectorNotice.ExportPackFailed -> stringResource(Res.string.pack_notice_export_failed)

    PackInspectorNotice.SaveFailed -> stringResource(Res.string.pack_notice_save_failed)

    PackInspectorNotice.LoadFailed -> stringResource(Res.string.pack_notice_load_failed)
}

@Composable
internal fun ActivePackUiState.localizedVersion(): String = version?.let {
    stringResource(Res.string.pack_meta_version, it)
} ?: versionLabel

@Composable
internal fun ActivePackUiState.localizedSchema(): String = schemaVersion?.let {
    stringResource(Res.string.pack_meta_schema, it)
} ?: schemaLabel

@Composable
internal fun ActivePackUiState.localizedRecordCount(): String = recordCount?.let {
    pluralStringResource(Res.plurals.pack_meta_records, it, it)
} ?: recordCountLabel

@Composable
internal fun ActivePackUiState.localizedChecked(): String = packCheckedOn?.let {
    stringResource(Res.string.pack_meta_checked, formatLocalDate(it))
} ?: checkedLabel

@Composable
internal fun ActivePackUiState.localizedSourceReviewRange(): String {
    val first = earliestRecordCheckedOn
    val last = latestRecordCheckedOn
    return when {
        recordCount == null -> sourceReviewRangeLabel

        first == null || last == null -> stringResource(Res.string.pack_meta_no_review_dates)

        first == last -> stringResource(Res.string.pack_meta_records_reviewed_on, formatLocalDate(first))

        else -> stringResource(
            Res.string.pack_meta_records_reviewed_range,
            formatLocalDate(first),
            formatLocalDate(last),
        )
    }
}

@Composable
internal fun ActivePackUiState.localizedReviewStatus(): String = sourceReviewDueCount?.let { due ->
    if (due == 0) {
        stringResource(Res.string.pack_meta_all_current, DEFAULT_SOURCE_REVIEW_WINDOW_DAYS)
    } else {
        pluralStringResource(Res.plurals.pack_meta_review_due, due, due)
    }
} ?: sourceReviewStatusLabel

@Composable
internal fun ActivePackUiState.localizedImported(): String = importedAtEpochMillis?.let {
    stringResource(Res.string.pack_meta_imported, it.asLocalizedDate())
} ?: importedLabel

@Composable
internal fun PendingPackUpdateUiState.localizedVersionTransition(): String = candidateVersion?.let { next ->
    previousVersion?.let { previous ->
        stringResource(Res.string.pack_meta_version_transition, previous, next)
    } ?: stringResource(Res.string.pack_meta_new_version, next)
} ?: versionTransitionLabel

@Composable
internal fun PackOpportunityUiState.localizedCategory(): String = category?.localizedCategory() ?: categoryLabel

@Composable
private fun OpportunityCategory.localizedCategory(): String = stringResource(
    when (this) {
        OpportunityCategory.SCHOLARSHIP -> Res.string.studio_enum_category_scholarship
        OpportunityCategory.INTERNSHIP -> Res.string.studio_enum_category_internship
        OpportunityCategory.ACADEMIC_PROGRAM -> Res.string.studio_enum_category_academic_program
        OpportunityCategory.STUDENT_CONTEST -> Res.string.studio_enum_category_student_contest
        OpportunityCategory.FELLOWSHIP -> Res.string.studio_enum_category_fellowship
    },
)

@Composable
internal fun PackOpportunityUiState.localizedChecked(): String {
    val date = lastCheckedOn ?: return checkedLabel
    return when (sourceFreshness) {
        SourceFreshness.CURRENT -> stringResource(
            Res.string.pack_record_review_current,
            formatLocalDate(date),
            DEFAULT_SOURCE_REVIEW_WINDOW_DAYS,
        )

        SourceFreshness.REVIEW_DUE -> stringResource(Res.string.pack_record_review_due, formatLocalDate(date))

        SourceFreshness.PREDATES_CYCLE -> stringResource(Res.string.pack_record_review_predates, formatLocalDate(date))

        SourceFreshness.FUTURE_DATED -> stringResource(Res.string.pack_record_review_future, formatLocalDate(date))

        null -> checkedLabel
    }
}

@Composable
internal fun RuleNodeUiState.localizedTypeLabel(): String = when (kind) {
    PackRuleKind.ALL -> stringResource(Res.string.pack_rule_all)

    PackRuleKind.ANY -> stringResource(Res.string.pack_rule_any)

    PackRuleKind.MANUAL_CHECK -> stringResource(Res.string.pack_rule_manual)

    PackRuleKind.ATOMIC -> {
        val key = factKey
        val comparison = operator
        if (key == null || comparison == null) {
            typeLabel
        } else {
            stringResource(
                Res.string.pack_rule_atomic,
                key.localizedFactKey(),
                comparison.localizedOperator(),
                expectedDisplayValue?.let { " $it" }.orEmpty(),
            )
        }
    }

    null -> typeLabel
}

@Composable
internal fun RuleNodeUiState.localizedSummary(): String = when (kind) {
    PackRuleKind.ALL -> childRuleCount?.let { stringResource(Res.string.pack_rule_all_summary, it) } ?: summary
    PackRuleKind.ANY -> childRuleCount?.let { stringResource(Res.string.pack_rule_any_summary, it) } ?: summary
    else -> summary
}

@Composable
private fun FactKey.localizedFactKey(): String = stringResource(
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
private fun Operator.localizedOperator(): String = stringResource(
    when (this) {
        Operator.EXISTS -> Res.string.pack_operator_exists
        Operator.EQUALS -> Res.string.pack_operator_equals
        Operator.NOT_EQUALS -> Res.string.pack_operator_not_equals
        Operator.GREATER_THAN_OR_EQUAL -> Res.string.pack_operator_gte
        Operator.LESS_THAN_OR_EQUAL -> Res.string.pack_operator_lte
        Operator.IN_SET -> Res.string.pack_operator_in_set
        Operator.CONTAINS_ANY -> Res.string.pack_operator_contains_any
        Operator.CONTAINS_ALL -> Res.string.pack_operator_contains_all
        Operator.DATE_BEFORE -> Res.string.pack_operator_before
        Operator.DATE_AFTER -> Res.string.pack_operator_after
    },
)
