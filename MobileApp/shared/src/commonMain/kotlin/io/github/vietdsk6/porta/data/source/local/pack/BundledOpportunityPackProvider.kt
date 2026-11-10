package io.github.vietdsk6.porta.data.source.local.pack

/**
 * Produces the independently curated opportunity pack that ships with Porta. The pack remains
 * available offline so failed or experimental imports can always be rolled back to known-good data.
 */
class BundledOpportunityPackProvider(
    private val codec: OpportunityPackCodec,
) {
    fun rawJson(): String = codec.encode(PortaContestPackData.create())

    /** Refresh only an older bundled pack; a user-imported catalog stays active. */
    fun shouldInstallOver(activePackId: String?, activeVersion: Int?): Boolean = activePackId == null ||
        activePackId == LEGACY_SYNTHETIC_PACK_ID ||
        (activePackId == PACK_ID && (activeVersion ?: 0) < PACK_VERSION)

    companion object {
        const val PACK_ID = "porta-curated-opportunities-2026-10-21"
        const val PACK_VERSION = 4
        const val LEGACY_SYNTHETIC_PACK_ID = "porta-synthetic-demo"
    }
}
