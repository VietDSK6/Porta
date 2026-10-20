package io.github.vietdsk6.porta.domain.usecase

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EligibilityReceipt
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.RuleReceipt
import io.github.vietdsk6.porta.domain.model.RuleResult
import kotlinx.datetime.LocalDate

class EligibilityEvaluator {
    fun evaluate(
        profile: ProfileSnapshot,
        opportunity: Opportunity,
        today: LocalDate,
    ): EligibilityReceipt {
        val evaluation = evaluateRule(profile, opportunity.eligibilityRule)
        val state = when {
            opportunity.deadline is Deadline.Known && opportunity.deadline.date < today -> {
                EligibilityState.CLOSED
            }

            evaluation.result == RuleResult.PASS && opportunity.deadline !is Deadline.Known -> {
                EligibilityState.NEEDS_INFO
            }

            evaluation.result == RuleResult.PASS -> EligibilityState.OPEN

            evaluation.result == RuleResult.UNKNOWN || evaluation.result == RuleResult.REVIEW -> {
                EligibilityState.NEEDS_INFO
            }

            evaluation.receipts.any {
                it.result == RuleResult.FAIL &&
                    it.actionability in setOf(Actionability.CONTROLLABLE, Actionability.TIME_DEPENDENT)
            } -> EligibilityState.HAS_GAPS

            else -> EligibilityState.NOT_ACTIONABLE
        }

        return EligibilityReceipt(
            opportunityId = opportunity.id,
            state = state,
            rootResult = evaluation.result,
            rules = evaluation.receipts,
            deadline = opportunity.deadline,
            lastCheckedOn = opportunity.lastCheckedOn,
        )
    }

    private fun evaluateRule(profile: ProfileSnapshot, rule: RuleNode): Evaluation = when (rule) {
        is RuleNode.All -> {
            val children = rule.children.map { evaluateRule(profile, it) }
            Evaluation(
                result = aggregateAll(children.map(Evaluation::result)),
                receipts = children.flatMap(Evaluation::receipts),
            )
        }

        is RuleNode.Any -> {
            val children = rule.children.map { evaluateRule(profile, it) }
            Evaluation(
                result = aggregateAny(children.map(Evaluation::result)),
                receipts = children.flatMap(Evaluation::receipts),
            )
        }

        is RuleNode.Atomic -> evaluateAtomic(profile, rule)

        is RuleNode.ManualCheck -> Evaluation(
            result = RuleResult.REVIEW,
            receipts = listOf(
                RuleReceipt(
                    ruleId = rule.id,
                    result = RuleResult.REVIEW,
                    explanation = rule.explanation,
                    sourceNote = rule.sourceNote,
                ),
            ),
        )
    }

    private fun evaluateAtomic(profile: ProfileSnapshot, rule: RuleNode.Atomic): Evaluation {
        val actual = profile.facts[rule.factKey]?.value
        val result = when {
            rule.operator == Operator.EXISTS -> if (actual == null) RuleResult.UNKNOWN else RuleResult.PASS

            actual == null || rule.expected == null -> RuleResult.UNKNOWN

            else -> compare(actual, rule.expected, rule.operator)
                ?.let { matches -> if (matches) RuleResult.PASS else RuleResult.FAIL }
                ?: RuleResult.UNKNOWN
        }

        return Evaluation(
            result = result,
            receipts = listOf(
                RuleReceipt(
                    ruleId = rule.id,
                    result = result,
                    explanation = rule.explanation,
                    factKey = rule.factKey,
                    actual = actual,
                    expected = rule.expected,
                    sourceNote = rule.sourceNote,
                    actionability = rule.actionability,
                    actionTemplateId = rule.actionTemplateId,
                ),
            ),
        )
    }

    private fun compare(
        actual: FactValue,
        expected: FactValue,
        operator: Operator,
    ): Boolean? = when (operator) {
        Operator.EXISTS -> true

        Operator.EQUALS -> equivalent(actual, expected)

        Operator.NOT_EQUALS -> equivalent(actual, expected)?.not()

        Operator.GREATER_THAN_OR_EQUAL -> compareOrdered(actual, expected)?.let { it >= 0 }

        Operator.LESS_THAN_OR_EQUAL -> compareOrdered(actual, expected)?.let { it <= 0 }

        Operator.IN_SET -> when {
            actual is FactValue.EnumValue && expected is FactValue.EnumListValue -> {
                actual.value in expected.values
            }

            actual is FactValue.TextValue && expected is FactValue.EnumListValue -> {
                actual.value in expected.values
            }

            else -> null
        }

        Operator.CONTAINS_ANY -> if (
            actual is FactValue.EnumListValue && expected is FactValue.EnumListValue
        ) {
            actual.values.any(expected.values::contains)
        } else {
            null
        }

        Operator.CONTAINS_ALL -> if (
            actual is FactValue.EnumListValue && expected is FactValue.EnumListValue
        ) {
            actual.values.containsAll(expected.values)
        } else {
            null
        }

        Operator.DATE_BEFORE -> if (
            actual is FactValue.DateValue && expected is FactValue.DateValue
        ) {
            actual.value < expected.value
        } else {
            null
        }

        Operator.DATE_AFTER -> if (
            actual is FactValue.DateValue && expected is FactValue.DateValue
        ) {
            actual.value > expected.value
        } else {
            null
        }
    }

    private fun equivalent(actual: FactValue, expected: FactValue): Boolean? = when {
        actual is FactValue.NumberValue && expected is FactValue.NumberValue -> {
            if (actual.unit != expected.unit) null else actual.amount == expected.amount
        }

        actual is FactValue.DurationValue && expected is FactValue.DurationValue -> {
            if (actual.unit != expected.unit) null else actual.amount == expected.amount
        }

        actual::class != expected::class -> null

        else -> actual == expected
    }

    private fun compareOrdered(actual: FactValue, expected: FactValue): Int? = when {
        actual is FactValue.NumberValue && expected is FactValue.NumberValue -> {
            if (actual.unit != expected.unit) null else actual.amount.compareTo(expected.amount)
        }

        actual is FactValue.DurationValue && expected is FactValue.DurationValue -> {
            if (actual.unit != expected.unit) null else actual.amount.compareTo(expected.amount)
        }

        else -> null
    }

    private fun aggregateAll(results: List<RuleResult>): RuleResult = when {
        RuleResult.FAIL in results -> RuleResult.FAIL
        RuleResult.REVIEW in results -> RuleResult.REVIEW
        RuleResult.UNKNOWN in results -> RuleResult.UNKNOWN
        else -> RuleResult.PASS
    }

    private fun aggregateAny(results: List<RuleResult>): RuleResult = when {
        RuleResult.PASS in results -> RuleResult.PASS
        RuleResult.REVIEW in results -> RuleResult.REVIEW
        RuleResult.UNKNOWN in results -> RuleResult.UNKNOWN
        else -> RuleResult.FAIL
    }

    private data class Evaluation(
        val result: RuleResult,
        val receipts: List<RuleReceipt>,
    )
}
