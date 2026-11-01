package io.github.vietdsk6.porta.presentation

import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.presentation.screens.studentprofile.referencedOpportunityCount
import kotlin.test.Test
import kotlin.test.assertEquals

class StudentProfileProjectionTest {
    @Test
    fun `reference count distinguishes no provided keys thirteen and full catalog`() {
        val template = PortaSampleData.opportunities.first()
        val opportunities = (1..15).map { index ->
            val key = if (index <= 13) FactKey.GPA else FactKey.AGE
            template.copy(
                id = "programme-$index",
                title = "Programme $index",
                eligibilityRule = RuleNode.All(
                    id = "root-$index",
                    children = listOf(
                        RuleNode.Atomic(
                            id = "check-$index",
                            factKey = key,
                            operator = Operator.EXISTS,
                            explanation = "Provide $key",
                            actionability = Actionability.UNKNOWN,
                        ),
                        RuleNode.ManualCheck(
                            id = "manual-$index",
                            explanation = "Review other conditions",
                        ),
                    ),
                ),
            )
        }

        assertEquals(0, referencedOpportunityCount(opportunities, emptySet()))
        assertEquals(13, referencedOpportunityCount(opportunities, setOf(FactKey.GPA)))
        assertEquals(15, referencedOpportunityCount(opportunities, setOf(FactKey.GPA, FactKey.AGE)))
    }

    @Test
    fun `manual-only checks do not count a provided detail`() {
        val opportunity = PortaSampleData.opportunities.first().copy(
            eligibilityRule = RuleNode.ManualCheck(id = "manual", explanation = "Review manually"),
        )

        assertEquals(0, referencedOpportunityCount(listOf(opportunity), setOf(FactKey.GPA)))
    }
}
