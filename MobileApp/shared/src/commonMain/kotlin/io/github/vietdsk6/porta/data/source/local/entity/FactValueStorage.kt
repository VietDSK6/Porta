package io.github.vietdsk6.porta.data.source.local.entity

import io.github.vietdsk6.porta.domain.model.DurationUnit
import io.github.vietdsk6.porta.domain.model.FactValue
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal data class StoredFactValue(
    val type: String,
    val number: Double? = null,
    val text: String? = null,
    val boolean: Boolean? = null,
    val listJson: String? = null,
    val unit: String? = null,
    val durationAmount: Int? = null,
)

internal fun FactValue.toStored(): StoredFactValue = when (this) {
    is FactValue.BooleanValue -> StoredFactValue(type = TYPE_BOOLEAN, boolean = value)

    is FactValue.DateValue -> StoredFactValue(type = TYPE_DATE, text = value.toString())

    is FactValue.DurationValue -> StoredFactValue(
        type = TYPE_DURATION,
        durationAmount = amount,
        unit = unit.name,
    )

    is FactValue.EnumListValue -> StoredFactValue(
        type = TYPE_ENUM_LIST,
        listJson = Json.encodeToString(values.sorted()),
    )

    is FactValue.EnumValue -> StoredFactValue(type = TYPE_ENUM, text = value)

    is FactValue.NumberValue -> StoredFactValue(type = TYPE_NUMBER, number = amount, unit = unit)

    is FactValue.TextValue -> StoredFactValue(type = TYPE_TEXT, text = value)
}

internal fun StoredFactValue.toDomain(): FactValue = when (type) {
    TYPE_BOOLEAN -> FactValue.BooleanValue(requireNotNull(boolean))

    TYPE_DATE -> FactValue.DateValue(LocalDate.parse(requireNotNull(text)))

    TYPE_DURATION -> FactValue.DurationValue(
        amount = requireNotNull(durationAmount),
        unit = DurationUnit.valueOf(requireNotNull(unit)),
    )

    TYPE_ENUM_LIST -> FactValue.EnumListValue(
        Json.decodeFromString<List<String>>(requireNotNull(listJson)).toSet(),
    )

    TYPE_ENUM -> FactValue.EnumValue(requireNotNull(text))

    TYPE_NUMBER -> FactValue.NumberValue(requireNotNull(number), requireNotNull(unit))

    TYPE_TEXT -> FactValue.TextValue(requireNotNull(text))

    else -> error("Unsupported stored fact value type: $type")
}

private const val TYPE_BOOLEAN = "BOOLEAN"
private const val TYPE_DATE = "DATE"
private const val TYPE_DURATION = "DURATION"
private const val TYPE_ENUM_LIST = "ENUM_LIST"
private const val TYPE_ENUM = "ENUM"
private const val TYPE_NUMBER = "NUMBER"
private const val TYPE_TEXT = "TEXT"
