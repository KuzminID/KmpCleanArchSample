package ru.marwinka.kmpcleanarchsample.core

import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

sealed interface AppResult<out T> {
    data class Success<out T>(
        val value: T,
    ) : AppResult<T>

    data class Failure(
        val error: AppError,
    ) : AppResult<Nothing>
}

/**
 * Выполняет [block] в [context] и превращает исключение в [AppResult.Failure] с [AppError.Unknown],
 * записывая его в [logger]. Отмену корутины пробрасывает дальше, а не превращает в результат.
 *
 * Для сетевых вызовов используется `networkResultOf` из `core:network`: там исключения
 * типизируются в месте происхождения.
 *
 * Перехват `Throwable` — назначение функции, поэтому `TooGenericExceptionCaught` подавлен.
 */
@Suppress("TooGenericExceptionCaught")
suspend fun <T> appResultOf(
    logger: Logger,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> T,
): AppResult<T> =
    try {
        AppResult.Success(withContext(context) { block() })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        logger.error(TAG, "Unexpected error", e)
        AppResult.Failure(AppError.Unknown(e))
    }

private const val TAG = "AppResult"
