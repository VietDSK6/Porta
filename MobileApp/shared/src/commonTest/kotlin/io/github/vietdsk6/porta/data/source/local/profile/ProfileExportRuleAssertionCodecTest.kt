package io.github.vietdsk6.porta.data.source.local.profile

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileExportRuleAssertionCodecTest {
    @Test
    fun `schema v2 exports facts and rule assertions separately`() {
        val ruleRef = RuleRef(
            opportunityId = "cern-summer",
            ruleId = "full-period-availability",
        )
        val profile = ProfileSnapshot(
            facts = mapOf(
                FactKey.GPA to ProfileFact(
                    id = "default:gpa",
                    key = FactKey.GPA,
                    value = FactValue.NumberValue(8.6, "10.0 scale"),
                    actionability = Actionability.CONTROLLABLE,
                    updatedAtEpochMillis = 40L,
                ),
            ),
            ruleAssertions = mapOf(
                ruleRef to RuleAssertion(
                    ruleRef = ruleRef,
                    ruleFingerprint = "sha256:full-period-availability",
                    factKey = FactKey.AVAILABLE_FULL_PERIOD,
                    value = FactValue.BooleanValue(true),
                    evidence = EvidenceMetadata(
                        label = "University calendar",
                        reference = "calendar.pdf",
                        checkedAtEpochMillis = 41L,
                    ),
                    updatedAtEpochMillis = 42L,
                ),
            ),
        )

        val codec = ProfileExportCodec()
        val encoded = codec.encode(
            profile = profile,
            exportedAtEpochMillis = 50L,
        )
        val export = Json.parseToJsonElement(encoded).jsonObject

        assertEquals(2, export.getValue("schemaVersion").jsonPrimitive.content.toInt())
        assertEquals("GPA", export.getValue("facts").jsonArray.single().jsonObject.getValue("key").jsonPrimitive.content)
        val assertion = export.getValue("ruleAssertions").jsonArray.single().jsonObject
        assertEquals("cern-summer", assertion.getValue("opportunityId").jsonPrimitive.content)
        assertEquals("full-period-availability", assertion.getValue("ruleId").jsonPrimitive.content)
        assertEquals("boolean", assertion.getValue("valueType").jsonPrimitive.content)
        assertTrue(export.getValue("warning").jsonPrimitive.content.contains("exact opportunity rule"))
        assertEquals(profile, codec.decode(encoded))
    }
}
