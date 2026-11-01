package io.github.vietdsk6.porta.util

import io.github.vietdsk6.porta.data.source.local.DatabaseProvider
import io.github.vietdsk6.porta.data.source.local.DatabaseProviderImpl
import io.github.vietdsk6.porta.data.source.local.databaseModule
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProvider
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProviderImpl
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.file.FileManagerImpl
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    includes(databaseModule)
    singleOf(::DatabaseProviderImpl) bind DatabaseProvider::class
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factory<FileManager> { FileManagerImpl() }
}

internal actual fun onApplicationStartPlatformSpecific() {
    FileKit.init("io.github.vietdsk6.porta")
}

actual fun getPlatform(): Platform = Platform.Desktop

internal actual val isDebug: Boolean
    get() = System.getProperty("app.debug") == "true" ||
        System.getenv("APP_DEBUG") == "true"

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO
