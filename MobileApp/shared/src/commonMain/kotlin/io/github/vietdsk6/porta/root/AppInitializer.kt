package io.github.vietdsk6.porta.root

import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.source.local.pack.BundledOpportunityPackProvider
import io.github.vietdsk6.porta.util.ApplicationScope
import io.github.vietdsk6.porta.util.isDebug
import io.github.vietdsk6.porta.util.logging.AppLogger
import io.github.vietdsk6.porta.util.onApplicationStartPlatformSpecific
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/** Starts dependency injection and installs Porta's bundled local opportunity pack. */
object AppInitializer {

    fun initialize(onKoinStart: KoinApplication.() -> Unit) {
        startKoin {
            onKoinStart()
            modules(appModules)
            onApplicationStart()
        }
    }

    private fun KoinApplication.onApplicationStart() {
        onApplicationStartPlatformSpecific()
        AppLogger.initialize(isDebug = isDebug)
        initializeBundledOpportunityPack()
    }
}

private fun KoinApplication.initializeBundledOpportunityPack() {
    val applicationScope by koin.inject<ApplicationScope>()
    val packRepository by koin.inject<OpportunityPackRepository>()
    val bundledPackProvider by koin.inject<BundledOpportunityPackProvider>()
    applicationScope.launch {
        packRepository.getActive().fold(
            onSuccess = { activePack ->
                if (
                    activePack == null ||
                    activePack.metadata.id == BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID
                ) {
                    packRepository.importPack(bundledPackProvider.rawJson())
                        .onFailure { AppLogger.e("Could not install bundled opportunity pack", it) }
                }
            },
            onFailure = { AppLogger.e("Could not read active opportunity pack", it) },
        )
    }
}
