package io.github.vietdsk6.porta.util

import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module

sealed interface Platform {
    data object Android : Platform
    data object Ios : Platform
    data object Desktop : Platform
    data object Web : Platform
}

expect fun getPlatform(): Platform

internal expect val platformModule: Module
internal expect fun onApplicationStartPlatformSpecific()
internal expect val isDebug: Boolean

expect val defaultAsyncDispatcher: CoroutineDispatcher
