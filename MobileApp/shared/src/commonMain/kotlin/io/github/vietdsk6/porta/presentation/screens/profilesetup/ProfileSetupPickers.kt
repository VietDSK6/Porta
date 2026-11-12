package io.github.vietdsk6.porta.presentation.screens.profilesetup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.profile_setup_birth_date
import io.github.vietdsk6.porta.generated.resources.profile_setup_birth_hint
import io.github.vietdsk6.porta.generated.resources.profile_setup_cancel
import io.github.vietdsk6.porta.generated.resources.profile_setup_choose_country
import io.github.vietdsk6.porta.generated.resources.profile_setup_choose_date
import io.github.vietdsk6.porta.generated.resources.profile_setup_clear
import io.github.vietdsk6.porta.generated.resources.profile_setup_country_example
import io.github.vietdsk6.porta.generated.resources.profile_setup_country_search
import io.github.vietdsk6.porta.generated.resources.profile_setup_country_value
import io.github.vietdsk6.porta.generated.resources.profile_setup_no
import io.github.vietdsk6.porta.generated.resources.profile_setup_not_provided
import io.github.vietdsk6.porta.generated.resources.profile_setup_results
import io.github.vietdsk6.porta.generated.resources.profile_setup_selected_value
import io.github.vietdsk6.porta.generated.resources.profile_setup_use_date
import io.github.vietdsk6.porta.presentation.locale.formatLocalDate
import io.github.vietdsk6.porta.util.time.CurrentLocalDateProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
internal fun CountrySelector(
    label: String,
    selectedCode: String,
    testTag: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCountry = countryForStoredValue(selectedCode)
    OutlinedButton(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp).testTag(testTag),
        onClick = { expanded = true },
    ) {
        Text(
            text = selectedCountry?.let { stringResource(Res.string.profile_setup_country_value, label, it.name) } ?: stringResource(Res.string.profile_setup_country_value, label, stringResource(Res.string.profile_setup_not_provided)),
            modifier = Modifier.fillMaxWidth(),
            color = if (selectedCountry == null) AppTheme.colors.text.secondary else AppTheme.colors.text.primary,
        )
    }
    if (expanded) {
        CountryPickerDialog(
            label = label,
            selectedCode = selectedCountry?.alpha2,
            onDismiss = { expanded = false },
            onSelected = {
                onSelected(it)
                expanded = false
            },
        )
    }
}

@Composable
private fun CountryPickerDialog(
    label: String,
    selectedCode: String?,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    var query by rememberSaveable(label) { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    val matches = remember(query) {
        val normalized = query.trim()
        ISO_COUNTRIES.filter { country ->
            normalized.isBlank() ||
                country.name.contains(normalized, ignoreCase = true) ||
                country.alpha2.contains(normalized, ignoreCase = true)
        }
    }
    Dialog(onDismissRequest = onDismiss) {
        BoxWithConstraints(Modifier.fillMaxWidth().imePadding()) {
            val dialogHeight = maxHeight.coerceAtMost(560.dp)
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).heightIn(max = dialogHeight),
                shape = RoundedCornerShape(20.dp),
                color = AppTheme.colors.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.heightIn(max = dialogHeight).padding(AppTheme.spacing.defaultSpacing),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(Res.string.profile_setup_choose_country, label),
                            modifier = Modifier.weight(1f),
                            style = AppTheme.typography.h6,
                            color = AppTheme.colors.text.primary,
                        )
                        TextButton(onClick = onDismiss, modifier = Modifier.testTag("country-cancel")) {
                            Text(stringResource(Res.string.profile_setup_cancel))
                        }
                    }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().focusRequester(searchFocus).testTag("country-search"),
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(Res.string.profile_setup_country_search)) },
                        placeholder = { Text(stringResource(Res.string.profile_setup_country_example)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    )
                    Text(
                        text = stringResource(Res.string.profile_setup_results, matches.size),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.text.secondary,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).testTag("country-results"),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (query.isBlank()) {
                            item {
                                CountryPickerRow(
                                    text = stringResource(Res.string.profile_setup_not_provided),
                                    selected = selectedCode == null,
                                    onClick = { onSelected("") },
                                )
                            }
                        }
                        items(matches, key = IsoCountry::alpha2) { country ->
                            CountryPickerRow(
                                text = country.displayName,
                                selected = selectedCode == country.alpha2,
                                onClick = { onSelected(country.alpha2) },
                                testTag = "country-${country.alpha2.lowercase()}",
                            )
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { searchFocus.requestFocus() }
}

@Composable
private fun CountryPickerRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String = "",
) {
    Text(
        text = if (selected) stringResource(Res.string.profile_setup_selected_value, text) else text,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.colors.text.primary,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BirthDateSelector(
    selectedDate: LocalDate?,
    hasStoredValue: Boolean,
    latestDate: LocalDate?,
    error: String?,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(Res.string.profile_setup_birth_date), style = AppTheme.typography.bodyMedium, color = AppTheme.colors.text.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 52.dp).testTag("profile-date-of-birth"),
                onClick = { expanded = true },
            ) {
                Text(selectedDate?.let(::displayBirthDate) ?: stringResource(Res.string.profile_setup_choose_date), modifier = Modifier.fillMaxWidth())
            }
            if (hasStoredValue) {
                TextButton(onClick = { onSelected("") }, modifier = Modifier.testTag("profile-date-clear")) {
                    Text(stringResource(Res.string.profile_setup_clear))
                }
            }
        }
        Text(
            text = error ?: stringResource(Res.string.profile_setup_birth_hint),
            color = if (error == null) AppTheme.colors.text.secondary else AppTheme.colors.status.error,
            style = AppTheme.typography.bodySmall,
        )
    }
    if (expanded) {
        val today = latestDate ?: remember { CurrentLocalDateProvider().today() }
        val initialDate = selectedDate ?: LocalDate(today.year - 20, 1, 1)
        val selectableBirthDates = remember(today) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= today.toUtcMillis()

                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.toUtcMillis(),
            initialDisplayedMonthMillis = initialDate.toUtcMillis(),
            selectableDates = selectableBirthDates,
        )
        DatePickerDialog(
            onDismissRequest = { expanded = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis?.let { it <= today.toUtcMillis() } == true,
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onSelected(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date.toString())
                        }
                        expanded = false
                    },
                    modifier = Modifier.testTag("profile-date-confirm"),
                ) { Text(stringResource(Res.string.profile_setup_use_date)) }
            },
            dismissButton = {
                TextButton(onClick = { expanded = false }) { Text(stringResource(Res.string.profile_setup_cancel)) }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun LocalDate.toUtcMillis(): Long = toEpochDays() * 86_400_000L

private fun displayBirthDate(date: LocalDate): String = formatLocalDate(date)
