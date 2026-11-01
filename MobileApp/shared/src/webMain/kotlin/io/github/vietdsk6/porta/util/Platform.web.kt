package io.github.vietdsk6.porta.util

import io.github.vietdsk6.porta.data.source.local.webDatabaseModule
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProvider
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProviderImpl
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.file.FileManagerImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    includes(webDatabaseModule)
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factory<FileManager> { FileManagerImpl() }
}

internal actual fun onApplicationStartPlatformSpecific() = Unit

actual fun getPlatform(): Platform = Platform.Web

internal actual val isDebug: Boolean
    get() = false

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.Default
