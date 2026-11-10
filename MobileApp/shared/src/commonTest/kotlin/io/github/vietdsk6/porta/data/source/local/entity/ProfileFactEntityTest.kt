package io.github.vietdsk6.porta.data.source.local.entity

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFactEntityTest {
    @Test
    fun `profile fact preserves local evidence attachment metadata`() {
        val fact = ProfileFact(
            id = "fact-1",
            profileId = "student-1",
            key = FactKey.IELTS_SCORE,
            value = FactValue.NumberValue(amount = 7.5, unit = "band"),
            provenance = FactProvenance.EVIDENCE_BACKED,
            actionability = Actionability.CONTROLLABLE,
            evidence = EvidenceMetadata(
                label = "IELTS Test Report Form",
                reference = "TRF 12345",
                checkedAtEpochMillis = 98L,
                attachment = EvidenceAttachmentMetadata(
                    displayName = "ielts-score.pdf",
                    mimeType = "application/pdf",
                    byteSize = 8_192L,
                    storageKey = "student-1/ielts-score.pdf",
                ),
            ),
            updatedAtEpochMillis = 123L,
        )

        val restored = fact.toEntity().toModel()

        assertEquals(fact, restored)
    }
}
