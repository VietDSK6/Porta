package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.fingerprint
import kotlin.test.Test
import kotlin.test.assertEquals

class DslPackEquivalenceTest {
    @Test
    fun dslPackPreservesEveryCuratedRecordAndRuleFingerprint() {
        val originalJson = checkNotNull(javaClass.getResourceAsStream("/porta_bundled_pack_golden.json")) {
            "The pre-DSL bundled pack fixture must be checked in."
        }.bufferedReader().use { it.readText() }.trim()
        val codec = OpportunityPackCodec()
        val original = codec.decode(originalJson).getOrThrow()
        val dsl = BundledPack.create()

        assertEquals(15, original.opportunities.size)
        assertEquals(original, dsl, "The DSL pack must preserve every serialized field and record order.")
        assertEquals(originalJson, codec.encode(dsl).trim(), "The exported JSON must remain byte-for-byte stable.")

        val oldFingerprints = original.opportunities.associate { opportunity ->
            opportunity.id to opportunity.toDomain().eligibilityRule.atomicFingerprints()
        }
        val newFingerprints = dsl.opportunities.associate { opportunity ->
            opportunity.id to opportunity.toDomain().eligibilityRule.atomicFingerprints()
        }
        assertEquals(oldFingerprints, newFingerprints, "Saved scoped assertions must remain valid.")
    }
}

private fun RuleNode.atomicFingerprints(): Map<String, String> = when (this) {
    is RuleNode.All -> children.flatMap { it.atomicFingerprints().entries }.associate { it.key to it.value }
    is RuleNode.Any -> children.flatMap { it.atomicFingerprints().entries }.associate { it.key to it.value }
    is RuleNode.Atomic -> mapOf(id to fingerprint())
    is RuleNode.ManualCheck -> emptyMap()
}
