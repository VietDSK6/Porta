package io.github.vietdsk6.porta.data.source.local.profile

import io.github.vietdsk6.porta.domain.model.FactValue

/** Stable export text for human-readable metadata; independent of the interface language. */
internal fun FactValue.exportValue(): String = when (this) {
    is FactValue.BooleanValue -> if (value) "Yes" else "No"

    is FactValue.DateValue -> value.toString()

    is FactValue.DurationValue -> "$amount ${unit.name.lowercase()}"

    is FactValue.EnumListValue -> values.sorted().joinToString()

    is FactValue.EnumValue -> value

    is FactValue.NumberValue -> if (unit.endsWith(" scale")) {
        "${amount.exportNumber(unit)} / ${unit.removeSuffix(" scale")}"
    } else {
        "${amount.exportNumber(unit)} $unit".trim()
    }

    is FactValue.TextValue -> value
}

private fun Double.exportNumber(unit: String): String = when {
    unit == "band" -> toString()
    toLong().toDouble() == this -> toLong().toString()
    else -> toString()
}
