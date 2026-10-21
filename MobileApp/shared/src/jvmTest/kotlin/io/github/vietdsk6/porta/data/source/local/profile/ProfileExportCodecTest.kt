package io.github.vietdsk6.porta.data.source.local.profile

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import kotlin.test.Test
import kotlin.test.assertContains

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
        assertContains(json, "Evidence binary files are not included")
    }
}
