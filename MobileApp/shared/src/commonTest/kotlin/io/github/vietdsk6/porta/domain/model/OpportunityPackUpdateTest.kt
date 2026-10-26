package io.github.vietdsk6.porta.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class OpportunityPackUpdateTest {
    @Test
    fun `pack diff reports added removed changed and unchanged records`() {
        val previous = document(
            version = 1,
            opportunities = listOf(
                opportunity("removed"),
                opportunity("changed"),
                opportunity("unchanged"),
            ),
        )
        val candidate = document(
            version = 2,
            opportunities = listOf(
                opportunity("added"),
                opportunity("changed").copy(title = "Changed title"),
                opportunity("unchanged"),
            ),
        )

        val diff = previous.diffTo(candidate)

        assertEquals(setOf("added"), diff.addedOpportunityIds)
        assertEquals(setOf("removed"), diff.removedOpportunityIds)
        assertEquals(setOf("changed"), diff.changedOpportunityIds)
        assertEquals(setOf("unchanged"), diff.unchangedOpportunityIds)
    }

    @Test
    fun `changed or removed rules make stored assertions stale without deleting them`() {
        val original = opportunity("door")
        val originalRule = original.eligibilityRule as RuleNode.Atomic
        val ruleRef = RuleRef(original.id, originalRule.id)
        val assertion = RuleAssertion(
            ruleRef = ruleRef,
            ruleFingerprint = originalRule.fingerprint(),
            factKey = originalRule.factKey,
            value = FactValue.BooleanValue(true),
        )
        val profile = ProfileSnapshot(ruleAssertions = mapOf(ruleRef to assertion))
        val matchingPack = document(version = 1, opportunities = listOf(original))
        val changedPack = document(
            version = 2,
            opportunities = listOf(
                original.copy(
                    eligibilityRule = originalRule.copy(
                        explanation = "Updated official wording that requires confirmation again.",
                    ),
                ),
            ),
        )

        assertEquals(emptyList(), profile.staleRuleAssertionsFor(matchingPack))
        assertEquals(listOf(assertion), profile.staleRuleAssertionsFor(changedPack))
        assertEquals(listOf(assertion), profile.staleRuleAssertionsFor(document(2, emptyList())))
        assertEquals(assertion, profile.ruleAssertions.getValue(ruleRef))
    }

    private fun document(
        version: Int,
        opportunities: List<Opportunity>,
    ): OpportunityPackDocument = OpportunityPackDocument(
        metadata = OpportunityPack(
            id = "test-pack",
            name = "Test pack",
            publisher = "Porta tests",
            schemaVersion = 2,
            version = version,
            rawJson = "{}",
            lastCheckedOn = LocalDate(2026, 10, 20),
            importedAtEpochMillis = 0L,
            isActive = false,
        ),
        opportunities = opportunities,
    )

    private fun opportunity(id: String): Opportunity = Opportunity(
        id = id,
        title = "Door $id",
        provider = "Test provider",
        category = OpportunityCategory.INTERNSHIP,
        cycle = "test-only",
        sourceUrl = "https://example.invalid/$id",
        lastCheckedOn = LocalDate(2026, 10, 20),
        deadline = Deadline.Tba,
        eligibilityRule = RuleNode.Atomic(
            id = "availability",
            factKey = FactKey.AVAILABLE_FULL_PERIOD,
            operator = Operator.EQUALS,
            expected = FactValue.BooleanValue(true),
            explanation = "Confirm availability.",
            sourceNote = "Synthetic test source.",
            actionability = Actionability.CONTROLLABLE,
        ),
    )
}
