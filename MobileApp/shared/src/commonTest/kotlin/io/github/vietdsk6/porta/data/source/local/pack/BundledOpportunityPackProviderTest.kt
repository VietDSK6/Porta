package io.github.vietdsk6.porta.data.source.local.pack

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BundledOpportunityPackProviderTest {
    @Test
    fun `bundled demo is a valid executable five-record pack`() {
        val codec = OpportunityPackCodec()
        val result = OpportunityPackParser(codec, OpportunityPackValidator()).parse(
            rawJson = BundledOpportunityPackProvider(codec).rawJson(),
            importedAtEpochMillis = 42L,
        )

        assertTrue(result.isValid)
        assertEquals(BundledOpportunityPackProvider.PACK_ID, result.document?.metadata?.id)
        assertEquals(5, result.document?.opportunities?.size)
        assertTrue(result.document?.opportunities?.all { it.eligibilityRule.id.isNotBlank() } == true)
    }
}
