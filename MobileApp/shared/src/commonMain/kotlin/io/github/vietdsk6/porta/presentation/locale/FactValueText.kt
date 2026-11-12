package io.github.vietdsk6.porta.presentation.locale

import io.github.vietdsk6.porta.domain.model.DurationUnit
import io.github.vietdsk6.porta.domain.model.FactValue

/** Human-readable values belong to presentation and follow Porta's selected language. */
fun FactValue.displayValue(): String {
    val vietnamese = AppLanguageState.effectiveLanguageTag.startsWith("vi")
    return when (this) {
        is FactValue.BooleanValue -> if (value) {
            if (vietnamese) "Có" else "Yes"
        } else {
            if (vietnamese) "Không" else "No"
        }

        is FactValue.DateValue -> formatLocalDate(value)

        is FactValue.DurationValue -> "$amount ${unit.localized(vietnamese)}"

        is FactValue.EnumListValue -> values.sorted().joinToString()

        is FactValue.EnumValue -> value

        is FactValue.NumberValue -> if (unit.endsWith(" scale")) {
            "${amount.displayNumber(unit)} / ${unit.removeSuffix(" scale")}"
        } else {
            "${amount.displayNumber(unit)} ${unit.localized(vietnamese)}".trim()
        }

        is FactValue.TextValue -> value
    }
}

private fun DurationUnit.localized(vietnamese: Boolean): String = when (this) {
    DurationUnit.HOURS -> if (vietnamese) "giờ" else "hours"
    DurationUnit.DAYS -> if (vietnamese) "ngày" else "days"
    DurationUnit.MONTHS -> if (vietnamese) "tháng" else "months"
    DurationUnit.YEARS -> if (vietnamese) "năm" else "years"
}

private fun String.localized(vietnamese: Boolean): String = if (vietnamese) {
    when (this) {
        "years" -> "năm"
        "months" -> "tháng"
        "days" -> "ngày"
        else -> this
    }
} else {
    this
}

private fun Double.displayNumber(unit: String): String = when {
    unit == "band" -> toString()
    toLong().toDouble() == this -> toLong().toString()
    else -> toString()
}
