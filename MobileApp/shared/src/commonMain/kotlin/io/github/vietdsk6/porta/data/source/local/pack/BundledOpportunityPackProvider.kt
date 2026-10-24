package io.github.vietdsk6.porta.data.source.local.pack

/**
 * Produces the independently curated opportunity pack that ships with Porta. The pack remains
 * available offline so failed or experimental imports can always be rolled back to known-good data.
 */
class BundledOpportunityPackProvider(
    private val codec: OpportunityPackCodec,
) {
    fun rawJson(): String = codec.encode(PortaContestPackData.create())

    companion object {
        const val PACK_ID = "porta-curated-opportunities-2026-10-21"
        const val PACK_VERSION = 2
        const val LEGACY_SYNTHETIC_PACK_ID = "porta-synthetic-demo"
    }
}
