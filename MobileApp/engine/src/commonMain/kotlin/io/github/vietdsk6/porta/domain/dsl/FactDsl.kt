package io.github.vietdsk6.porta.domain.dsl

import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator

/** Marker values prevent mixing Boolean, numeric, enum and duration operators. */
sealed interface FactField<out V : FactValue> {
    val key: FactKey
}

open class NumberField(override val key: FactKey) : FactField<FactValue.NumberValue>

open class BooleanField(override val key: FactKey) : FactField<FactValue.BooleanValue>

open class EnumField(override val key: FactKey) : FactField<FactValue.EnumValue>

open class DurationField(override val key: FactKey) : FactField<FactValue.DurationValue>

object IeltsScore : NumberField(FactKey.IELTS_SCORE)

object Gpa : NumberField(FactKey.GPA)

object Age : NumberField(FactKey.AGE)

object FirstDegree : BooleanField(FactKey.DEGREE_COMPLETED)

object Enrolled : BooleanField(FactKey.ENROLLED)

object Nationality : EnumField(FactKey.NATIONALITY)

object Experience : NumberField(FactKey.EXPERIENCE_MONTHS)

data class FactPredicate(
    val key: FactKey,
    val operator: Operator,
    val expected: FactValue?,
)

class NumberFactRef internal constructor(val field: NumberField) {
    infix fun atLeast(value: FactValue.NumberValue) = FactPredicate(field.key, Operator.GREATER_THAN_OR_EQUAL, value)

    infix fun atMost(value: FactValue.NumberValue) = FactPredicate(field.key, Operator.LESS_THAN_OR_EQUAL, value)

    infix fun equalTo(value: FactValue.NumberValue) = FactPredicate(field.key, Operator.EQUALS, value)
}

class BooleanFactRef internal constructor(val field: BooleanField) {
    fun isTrue() = FactPredicate(field.key, Operator.EQUALS, FactValue.BooleanValue(true))

    fun isFalse() = FactPredicate(field.key, Operator.EQUALS, FactValue.BooleanValue(false))
}

class EnumFactRef internal constructor(val field: EnumField) {
    infix fun oneOf(values: List<String>): FactPredicate {
        requireDsl(values.isNotEmpty(), "fact[${field.key}].oneOf", "At least one value is required.")
        return FactPredicate(field.key, Operator.IN_SET, FactValue.EnumListValue(values.toSet()))
    }

    infix fun equalTo(value: String) = FactPredicate(field.key, Operator.EQUALS, FactValue.EnumValue(value))
}

class DurationFactRef internal constructor(val field: DurationField) {
    infix fun atLeast(value: FactValue.DurationValue) = FactPredicate(field.key, Operator.GREATER_THAN_OR_EQUAL, value)

    infix fun atMost(value: FactValue.DurationValue) = FactPredicate(field.key, Operator.LESS_THAN_OR_EQUAL, value)
}

fun fact(field: NumberField) = NumberFactRef(field)

fun fact(field: BooleanField) = BooleanFactRef(field)

fun fact(field: EnumField) = EnumFactRef(field)

fun fact(field: DurationField) = DurationFactRef(field)

val Double.band: FactValue.NumberValue get() = FactValue.NumberValue(this, "band")

val Int.months: FactValue.NumberValue get() = FactValue.NumberValue(toDouble(), "months")

val Double.years: FactValue.NumberValue get() = FactValue.NumberValue(this, "years")
