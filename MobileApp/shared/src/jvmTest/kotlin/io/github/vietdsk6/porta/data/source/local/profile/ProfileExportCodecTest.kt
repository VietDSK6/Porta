package io.github.vietdsk6.porta.data.source.local.profile

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ProfileExportCodecTest {
    @Test
    fun `export lists sensitive metadata and excludes evidence binaries`() {
        val profile = ProfileSnapshot(
            facts = listOf(
                ProfileFact(
                    id = "default:ielts_score",
                    key = FactKey.IELTS_SCORE,
                    value = FactValue.NumberValue(7.0, "band"),
                    provenance = FactProvenance.EVIDENCE_BACKED,
                    actionability = Actionability.CONTROLLABLE,
                    evidence = EvidenceMetadata(
                        label = "Official score report",
                        reference = "local-reference-only",
                        attachment = EvidenceAttachmentMetadata(
                            displayName = "ielts-score.pdf",
                            mimeType = "application/pdf",
                            byteSize = 1_024L,
                            storageKey = "default/private-file-id.pdf",
                        ),
                    ),
                    updatedAtEpochMillis = 42L,
                ),
            ).associateBy(ProfileFact::key),
        )

        val json = ProfileExportCodec().encode(profile, exportedAtEpochMillis = 99L)

        assertContains(json, "Private profile metadata")
        assertContains(json, "IELTS_SCORE")
        assertContains(json, "7.0 band")
        assertContains(json, "Official score report")
        assertContains(json, "ielts-score.pdf")
        assertContains(json, "application/pdf")
        assertContains(json, "Evidence binary files are not included")
        assertFalse(json.contains("private-file-id"))

        val decodedAttachment = ProfileExportCodec().decode(json)
            .facts.getValue(FactKey.IELTS_SCORE)
            .evidence
            ?.attachment
        assertEquals("ielts-score.pdf", decodedAttachment?.displayName)
        assertEquals(1_024L, decodedAttachment?.byteSize)
        assertNull(decodedAttachment?.storageKey)
    }

    @Test
    fun `decode rejects duplicate fact keys instead of silently keeping the last value`() {
        val profile = ProfileSnapshot(
            facts = mapOf(
                FactKey.IELTS_SCORE to ProfileFact(
                    id = "default:ielts_score",
                    key = FactKey.IELTS_SCORE,
                    value = FactValue.NumberValue(7.0, "band"),
                ),
            ),
        )
        val codec = ProfileExportCodec()
        val exported = Json.parseToJsonElement(codec.encode(profile, 99L)).jsonObject
        val fact = exported.getValue("facts").jsonArray.single()
        val duplicated = buildJsonObject {
            exported.forEach { (key, value) -> put(key, value) }
            put("facts", JsonArray(listOf(fact, fact)))
        }.toString()

        assertFailsWith<IllegalArgumentException> { codec.decode(duplicated) }
    }

    @Test
    fun `decode requires an explicit supported schema version`() {
        val profile = ProfileSnapshot(
            facts = mapOf(
                FactKey.ENROLLED to ProfileFact(
                    id = "default:enrolled",
                    key = FactKey.ENROLLED,
                    value = FactValue.BooleanValue(true),
                ),
            ),
        )
        val codec = ProfileExportCodec()
        val exported = Json.parseToJsonElement(codec.encode(profile, 99L)).jsonObject
        assertEquals("2", exported.getValue("schemaVersion").jsonPrimitive.content)
        val withoutSchema = buildJsonObject {
            exported.filterKeys { it != "schemaVersion" }.forEach { (key, value) -> put(key, value) }
        }.toString()

        assertFailsWith<Exception> { codec.decode(withoutSchema) }
    }
}
