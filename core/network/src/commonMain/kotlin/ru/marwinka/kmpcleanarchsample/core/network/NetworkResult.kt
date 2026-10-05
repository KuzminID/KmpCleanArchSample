package ru.marwinka.kmpcleanarchsample.core.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import ru.marwinka.kmpcleanarchsample.core.AppError
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.core.Logger
import kotlin.coroutines.cancellation.CancellationException

/**
 * Выполняет сетевой вызов и типизирует ошибку в месте её происхождения: выше по стеку
 * маппинг исключений Ktor не дублируется. Отмену корутины пробрасывает дальше.
 * Неожиданные ошибки ([AppError.Unknown]) логируются как ошибки, сетевые — как отладочные.
 *
 * Перехват `Throwable` — назначение функции, поэтому `TooGenericExceptionCaught` подавлен.
 */
@Suppress("TooGenericExceptionCaught")
suspend fun <T> networkResultOf(
    logger: Logger,
    block: suspend () -> T,
): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: HttpRequestTimeoutException) {
        // Отдельная ветка до CancellationException: таймаут — сетевая ошибка, а не отмена.
        failure(logger, e)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        failure(logger, e)
    }

private fun failure(
    logger: Logger,
    throwable: Throwable,
): AppResult.Failure {
    val error = throwable.toAppError()
    if (error is AppError.Unknown) {
        logger.error(TAG, "Unexpected network error", throwable)
    } else {
        logger.debug(TAG, "Network call failed: $error")
    }
    return AppResult.Failure(error)
}

internal fun Throwable.toAppError(): AppError =
    when (this) {
        is ClientRequestException ->
            if (response.status == HttpStatusCode.NotFound) AppError.NotFound(this) else AppError.Unknown(this)
        is ServerResponseException -> AppError.Network(this)
        is HttpRequestTimeoutException -> AppError.Network(this)
        is IOException -> AppError.Network(this)
        else -> AppError.Unknown(this)
    }

private const val TAG = "Network"
