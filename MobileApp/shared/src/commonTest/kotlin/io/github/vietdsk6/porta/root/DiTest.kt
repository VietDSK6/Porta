package io.github.vietdsk6.porta.root

import io.github.vietdsk6.porta.data.source.local.pack.OpportunityPackCodec
import io.github.vietdsk6.porta.data.source.local.profile.ProfileExportCodec
import io.github.vietdsk6.porta.util.file.FileManager
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertNotNull

class DiTest {
    @Test
    fun productionGraphResolvesCodecsWithConfiguredJsonDefaults() {
        val application = koinApplication {
            modules(appModules)
        }

        try {
            assertNotNull(application.koin.get<OpportunityPackCodec>())
            assertNotNull(application.koin.get<ProfileExportCodec>())
            assertNotNull(application.koin.get<FileManager>())
        } finally {
            application.close()
        }
    }
}
