package io.github.vietdsk6.porta.util

import io.github.vietdsk6.porta.data.source.local.DatabaseProvider
import io.github.vietdsk6.porta.data.source.local.DatabaseProviderImpl
import io.github.vietdsk6.porta.data.source.local.databaseModule
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProvider
import io.github.vietdsk6.porta.data.source.preferences.PreferencesDataStoreProviderImpl
import io.github.vietdsk6.porta.util.file.FileManager
import io.github.vietdsk6.porta.util.file.FileManagerImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi

internal actual val platformModule: Module = module {
    includes(databaseModule)
    singleOf(::DatabaseProviderImpl) bind DatabaseProvider::class
    singleOf(::PreferencesDataStoreProviderImpl) bind PreferencesDataStoreProvider::class
    factory<FileManager> { FileManagerImpl() }
}

internal actual fun onApplicationStartPlatformSpecific() = Unit

actual fun getPlatform(): Platform = Platform.Ios

@OptIn(ExperimentalNativeApi::class)
internal actual val isDebug = kotlin.native.Platform.isDebugBinary

actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO
