package io.github.vietdsk6.porta.util

import io.github.vietdsk6.porta.util.logging.AppLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * App-lifetime coroutine scope for local work that must outlive any screen.
 * SupervisorJob so one failed child doesn't cancel the rest; uncaught errors are logged.
 */
class ApplicationScope : CoroutineScope {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLogger.e("Unknown exception in application scope", throwable)
    }

    override val coroutineContext: CoroutineContext
        get() = CoroutineScope(SupervisorJob() + defaultAsyncDispatcher + coroutineExceptionHandler).coroutineContext
}
