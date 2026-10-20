package io.github.vietdsk6.porta.subscription.api

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

suspend inline fun <R> runCatchingSuspend(block: suspend () -> R): Result<R> = try {
    Result.success(block())
} catch (e: Throwable) {
    currentCoroutineContext().ensureActive()
    Result.failure(e)
}
