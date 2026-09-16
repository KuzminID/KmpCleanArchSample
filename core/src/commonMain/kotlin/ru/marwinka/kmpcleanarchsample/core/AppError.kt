package ru.marwinka.kmpcleanarchsample.core

sealed class AppError(override val message: String, override val cause: Throwable? = null) : Throwable(message, cause) {
    class Network(cause: Throwable? = null) : AppError("Network error", cause)
    class NotFound(cause: Throwable? = null) : AppError("Not found", cause)
    class Unknown(cause: Throwable? = null) : AppError(cause?.message ?: "Unknown error", cause)
}

fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    else -> AppError.Unknown(this)
}

suspend fun <T> resultOf(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (t: Throwable) {
        if (t is kotlinx.coroutines.CancellationException) throw t
        Result.failure(t.toAppError())
    }
