package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.RuleReceipt
import io.github.vietdsk6.porta.domain.model.RuleResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PublishedCheckPathTest {
    @Test
    fun `passing published alternative hides the unused manual branch`() {
        val path = publishedCheckPath(
            RuleNode.Any("degree", listOf(degreeRule, manualRule)),
            listOf(receipt("degree-complete", RuleResult.PASS), receipt("final-year", RuleResult.REVIEW)),
        )

        assertEquals(setOf("degree-complete"), path.ruleIds)
        assertEquals(1, path.passed)
        assertEquals(0, path.review)
    }

    @Test
    fun `actionable failed branch remains visible before alternate manual route`() {
        val path = publishedCheckPath(
            RuleNode.Any("degree", listOf(degreeRule, manualRule)),
            listOf(receipt("degree-complete", RuleResult.FAIL), receipt("final-year", RuleResult.REVIEW)),
        )

        assertEquals(setOf("degree-complete"), path.ruleIds)
        assertEquals(1, path.failed)
    }

    @Test
    fun `remaining required manual condition is stated without full pass claim`() {
        val path = publishedCheckPath(
            RuleNode.All("root", listOf(degreeRule, manualRule)),
            listOf(receipt("degree-complete", RuleResult.PASS), receipt("final-year", RuleResult.REVIEW)),
        )

        assertTrue(path.hasOnlyManualRemaining)
        assertEquals(1, path.passed)
        assertEquals(1, path.review)
    }

    private fun receipt(id: String, result: RuleResult) = RuleReceipt(id, result, id)

    private val degreeRule = RuleNode.Atomic(
        id = "degree-complete",
        factKey = FactKey.DEGREE_COMPLETED,
        operator = Operator.EQUALS,
        expected = FactValue.BooleanValue(true),
        explanation = "Degree completed",
        actionability = Actionability.TIME_DEPENDENT,
    )
    private val manualRule = RuleNode.ManualCheck("final-year", "Final-year option")
}
