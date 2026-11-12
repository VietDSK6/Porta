package io.github.vietdsk6.porta.data.scenario

import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider

/** Presentation-facing access to the offline curated catalog. */
class BundledPackSource(
    private val provider: BundledOpportunityPackProvider,
) {
    fun rawJson(): String = provider.rawJson()

    fun isBundled(id: String): Boolean = id == BundledOpportunityPackProvider.PACK_ID
}
