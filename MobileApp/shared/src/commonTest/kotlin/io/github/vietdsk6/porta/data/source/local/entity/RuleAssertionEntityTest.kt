package io.github.vietdsk6.porta.data.source.local.entity

import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import kotlin.test.Test
import kotlin.test.assertEquals

class RuleAssertionEntityTest {
    @Test
    fun `rule assertion preserves scope typed value and evidence`() {
        val assertion = RuleAssertion(
            profileId = "student-1",
            ruleRef = RuleRef(
                opportunityId = "oecd-internship",
                ruleId = "availability-2027",
            ),
            ruleFingerprint = "sha256:availability-2027",
            factKey = FactKey.AVAILABLE_FULL_PERIOD,
            value = FactValue.EnumListValue(setOf("JUNE", "JULY", "AUGUST")),
            evidence = EvidenceMetadata(
                label = "Semester calendar",
                reference = "file://calendar.pdf",
                checkedAtEpochMillis = 98L,
                attachment = EvidenceAttachmentMetadata(
                    displayName = "calendar.pdf",
                    mimeType = "application/pdf",
                    byteSize = 4_096L,
                    storageKey = "student-1/calendar.pdf",
                ),
            ),
            updatedAtEpochMillis = 123L,
        )

        val restored = assertion.toEntity().toModel()

        assertEquals(assertion, restored)
    }
}
