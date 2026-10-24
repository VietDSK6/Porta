package io.github.vietdsk6.porta.util

import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import com.mmk.kmpnotifier.push.firebase.FirebasePush
import com.mmk.kmpnotifier.push.firebase.onApplicationDidReceiveRemoteNotification
import io.github.vietdsk6.porta.data.source.featureflag.FeatureFlagManager
import io.github.vietdsk6.porta.data.source.local.DatabaseProvider
import io.github.vietdsk6.porta.data.source.local.DatabaseProviderImpl
import io.github.vietdsk6.porta.data.source.local.databaseModule
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProvider
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProviderImpl
import io.github.vietdsk6.porta.presentation.components.ads.AdsManager
import io.github.vietdsk6.porta.presentation.components.ads.IosAdsDisplayer
import io.github.vietdsk6.porta.util.analytics.Analytics
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.file.FileManagerImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi

internal actual val platformModule: Module = module {
    includes(databaseModule)
    singleOf(::DatabaseProviderImpl) bind DatabaseProvider::class
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factory<FileManager> { FileManagerImpl() }
    factoryOf(::AppUtilImpl) bind AppUtil::class
}

internal fun swiftLibDependenciesModule(factory: SwiftLibDependencyFactory): Module = module {
    single { factory.provideFeatureFlagManagerImpl() } bind FeatureFlagManager::class
    single { factory.provideFirebaseAnalyticsImpl() } bind Analytics::class
    single { factory.provideAdsManagerImpl() } bind AdsManager::class
    single { factory.provideIosAdsDisplayer() } bind IosAdsDisplayer::class
}

internal actual fun onApplicationStartPlatformSpecific(enableRemoteNotifications: Boolean) {
    if (!enableRemoteNotifications) return
    KMPNotifier.initialize(NotificationPlatformConfiguration.Ios(askNotificationPermissionOnStart = false), FirebasePush)
}

// Bridge so the iOS app forwards remote-notification callbacks without the Swift side
// (or the exported framework) depending on KMPNotifier types directly.
object IosPushNotificationHandler {
    fun onApplicationDidReceiveRemoteNotification(userInfo: Map<Any?, *>) {
        KMPNotifier.onApplicationDidReceiveRemoteNotification(userInfo)
    }
}

actual fun getPlatform(): Platform = Platform.Ios

internal actual val isAndroid = false

@OptIn(ExperimentalNativeApi::class)
internal actual val isDebug = kotlin.native.Platform.isDebugBinary

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO
