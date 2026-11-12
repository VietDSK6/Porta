package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import androidx.compose.runtime.Composable
import io.github.vietdsk6.porta.generated.resources.*
import io.github.vietdsk6.porta.presentation.locale.toText
import org.jetbrains.compose.resources.stringResource

internal fun String.fileValidationCode(): ActualCopyCode = when (this) {
    "The evidence file must have a name." -> ActualCopyCode.FILE_NAME_REQUIRED
    "The evidence file is empty." -> ActualCopyCode.FILE_EMPTY
    "The evidence file must be 10 MB or smaller." -> ActualCopyCode.FILE_TOO_LARGE
    "Unsupported evidence file type. Choose an image, PDF, text, or Word document." -> ActualCopyCode.FILE_UNSUPPORTED
    "The evidence file type does not match its file name." -> ActualCopyCode.FILE_MIME_MISMATCH
    else -> ActualCopyCode.FILE_SELECTION_FAILED
}

internal fun String?.actualValidationCode(): ActualValidationCode? = when {
    this == null -> null
    this == "Enter one or more values separated by commas or new lines" -> ActualValidationCode.ENUM_LIST
    this == "Use an ISO date in YYYY-MM-DD format" -> ActualValidationCode.DATE
    this == "Use a whole duration of zero or more" -> ActualValidationCode.DURATION
    this == "Enter a number" -> ActualValidationCode.NUMBER
    this?.startsWith("Use the ") == true && this?.endsWith(" GPA scale") == true -> ActualValidationCode.GPA_SCALE
    this == "IELTS bands range from 0 to 9" -> ActualValidationCode.IELTS_RANGE
    this == "Use whole months from 0 to 600" -> ActualValidationCode.EXPERIENCE_MONTHS
    this == "Use zero or a positive number" -> ActualValidationCode.NONNEGATIVE
    else -> null
}

@Composable
internal fun ActualProfileUpdateUiState.localizedFactLabel(): String {
    val key = factKey ?: return factLabel
    val name = key.toText()
    return if (isProgrammeSpecific) stringResource(Res.string.actual_copy_programme_specific, name) else name
}

@Composable
internal fun ActualProfileUpdateUiState.localizedScope(): String? {
    if (scopeDescription == null) return null
    val programme = scopeProgrammeName ?: stringResource(Res.string.actual_copy_this_programme)
    val requirement = scopeRequirement ?: factKey?.toText() ?: scopeDescription
    return "$programme · $requirement"
}

@Composable
internal fun ActualProfileUpdateUiState.localizedCurrentValue(): String = if (currentValueMissing) {
    stringResource(Res.string.actual_copy_not_confirmed)
} else {
    currentValueLabel
}

@Composable
internal fun ActualProfileUpdateUiState.localizedScopeWarning(): String? {
    if (scopeWarning == null) return null
    return if (scopeWarningCode == ActualCopyCode.SCOPE_CHANGED) {
        stringResource(Res.string.actual_copy_scope_changed)
    } else {
        scopeWarning
    }
}

@Composable
internal fun ActualProfileUpdateUiState.localizedError(): String? {
    if (error == null) return null
    return errorCode?.toText() ?: error
}

@Composable
internal fun ActualProfileUpdateUiState.localizedEvidenceError(): String? {
    if (evidenceAttachmentError == null) return null
    return evidenceAttachmentErrorCode?.toText() ?: evidenceAttachmentError
}

@Composable
internal fun ActualProfileUpdateUiState.localizedValidationError(): String? {
    if (achievedValueError == null) return null
    return when (achievedValueErrorCode) {
        ActualValidationCode.ENUM_LIST -> stringResource(Res.string.actual_copy_enum_list)
        ActualValidationCode.DATE -> stringResource(Res.string.actual_copy_date)
        ActualValidationCode.DURATION -> stringResource(Res.string.actual_copy_duration)
        ActualValidationCode.NUMBER -> stringResource(Res.string.actual_copy_number)
        ActualValidationCode.GPA_SCALE -> stringResource(Res.string.actual_copy_gpa_scale, validationScale.orEmpty())
        ActualValidationCode.IELTS_RANGE -> stringResource(Res.string.actual_copy_ielts_range)
        ActualValidationCode.EXPERIENCE_MONTHS -> stringResource(Res.string.actual_copy_experience_months)
        ActualValidationCode.NONNEGATIVE -> stringResource(Res.string.actual_copy_nonnegative)
        null -> achievedValueError
    }
}

@Composable
internal fun ActualProfileUpdateUiState.localizedUnit(): String = when (unitLabel) {
    "days" -> stringResource(Res.string.actual_copy_unit_days)

    "months" -> stringResource(Res.string.actual_copy_unit_months)

    "years" -> stringResource(Res.string.actual_copy_unit_years)

    "hours" -> stringResource(Res.string.actual_copy_unit_hours)

    "band" -> stringResource(Res.string.actual_copy_unit_band)

    else -> if (unitLabel.endsWith(" scale")) {
        stringResource(Res.string.actual_copy_unit_scale, unitLabel.removeSuffix(" scale"))
    } else {
        unitLabel
    }
}

@Composable
private fun ActualCopyCode.toText(): String = stringResource(
    when (this) {
        ActualCopyCode.PLAN_CHANGED -> Res.string.actual_copy_plan_changed
        ActualCopyCode.RULE_CHANGED -> Res.string.actual_copy_rule_changed
        ActualCopyCode.INVALID_VALUE -> Res.string.actual_copy_invalid_value
        ActualCopyCode.PROFILE_READ_FAILED -> Res.string.actual_copy_profile_read_failed
        ActualCopyCode.FILE_COPY_FAILED -> Res.string.actual_copy_file_copy_failed
        ActualCopyCode.FACT_SAVE_FAILED -> Res.string.actual_copy_fact_save_failed
        ActualCopyCode.FILE_MISSING -> Res.string.actual_copy_file_missing
        ActualCopyCode.FILE_OPEN_FAILED -> Res.string.actual_copy_file_open_failed
        ActualCopyCode.SCOPE_CHANGED -> Res.string.actual_copy_scope_changed
        ActualCopyCode.FILE_SELECTION_FAILED -> Res.string.actual_copy_file_selection_failed
        ActualCopyCode.FILE_NAME_REQUIRED -> Res.string.actual_copy_file_name_required
        ActualCopyCode.FILE_EMPTY -> Res.string.actual_copy_file_empty
        ActualCopyCode.FILE_TOO_LARGE -> Res.string.actual_copy_file_too_large
        ActualCopyCode.FILE_UNSUPPORTED -> Res.string.actual_copy_file_unsupported
        ActualCopyCode.FILE_MIME_MISMATCH -> Res.string.actual_copy_file_mime_mismatch
    },
)
