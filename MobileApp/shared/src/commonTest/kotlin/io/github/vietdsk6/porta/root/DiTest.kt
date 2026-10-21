package io.github.vietdsk6.porta.root

import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

class DiTest {
    @Test
    fun productionGraphResolvesOpportunityPackCodec() {
        val application = koinApplication {
            modules(appModules)
        }

        try {
            assertNotNull(application.koin.get<OpportunityPackCodec>())
        } finally {
            application.close()
        }
    }
}
