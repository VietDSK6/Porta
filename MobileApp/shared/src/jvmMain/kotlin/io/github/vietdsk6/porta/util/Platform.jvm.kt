package io.github.vietdsk6.porta.util

import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import com.mmk.kmpnotifier.push.firebase.FirebasePush
import io.github.vietdsk6.porta.data.source.featureflag.FeatureFlagManager
import io.github.vietdsk6.porta.data.source.featureflag.NoImplFeatureFlagManager
import io.github.vietdsk6.porta.data.source.local.DatabaseProvider
import io.github.vietdsk6.porta.data.source.local.DatabaseProviderImpl
import io.github.vietdsk6.porta.data.source.local.databaseModule
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProvider
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProviderImpl
import io.github.vietdsk6.porta.presentation.components.ads.AdsManager
import io.github.vietdsk6.porta.presentation.components.ads.NoImplAdsManager
import io.github.vietdsk6.porta.util.analytics.Analytics
import io.github.vietdsk6.porta.util.analytics.NoImplAnalytics
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.file.FileManagerImpl
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    includes(databaseModule)
    singleOf(::DatabaseProviderImpl) bind DatabaseProvider::class
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factory<FileManager> { FileManagerImpl() }
    factoryOf(::AppUtilImpl) bind AppUtil::class
    single { NoImplFeatureFlagManager } bind FeatureFlagManager::class
    single { NoImplAnalytics } bind Analytics::class
    single { NoImplAdsManager } bind AdsManager::class
}

internal actual fun onApplicationStartPlatformSpecific() {
    KMPNotifier.initialize(NotificationPlatformConfiguration.Desktop(), FirebasePush)
    FileKit.init("io.github.vietdsk6.porta")
}

actual fun getPlatform(): Platform = Platform.Desktop

internal actual val isAndroid: Boolean
    get() = false
internal actual val isDebug: Boolean
    get() = System.getProperty("app.debug") == "true" ||
        System.getenv("APP_DEBUG") == "true"

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO
