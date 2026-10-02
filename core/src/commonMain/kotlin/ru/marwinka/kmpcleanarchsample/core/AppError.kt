package ru.marwinka.kmpcleanarchsample.core

sealed class AppError(
    override val message: String,
    override val cause: Throwable? = null,
) : Throwable(message, cause) {
    class Network(
        cause: Throwable? = null,
    ) : AppError("Network error", cause)

    class NotFound(
        cause: Throwable? = null,
    ) : AppError("Not found", cause)

    class Unknown(
        cause: Throwable? = null,
    ) : AppError(cause?.message ?: "Unknown error", cause)
}

fun Throwable.toAppError(): AppError =
    when (this) {
        is AppError -> this
        else -> AppError.Unknown(this)
    }

sealed interface AppResult<out T> {
    data class Success<out T>(
        val value: T,
    ) : AppResult<T>

    data class Failure(
        val error: AppError,
    ) : AppResult<Nothing>
}

/** Выполняет [block] и превращает исключение в [AppResult.Failure]; отмену корутины пробрасывает дальше. */
suspend fun <T> appResultOf(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (t: kotlinx.coroutines.CancellationException) {
        throw t
    } catch (t: Throwable) {
        AppResult.Failure(t.toAppError())
    }
