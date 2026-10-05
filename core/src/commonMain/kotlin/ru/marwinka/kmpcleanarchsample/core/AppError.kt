package ru.marwinka.kmpcleanarchsample.core

/**
 * Типизированная ошибка операции. Это данные, а не исключение: её нельзя бросить, только вернуть
 * в [AppResult.Failure]. Текста для пользователя здесь нет — его подбирает экран по типу ошибки.
 */
sealed interface AppError {
    val cause: Throwable?

    /** Нет соединения, таймаут или ответ сервера 5xx. */
    data class Network(
        override val cause: Throwable? = null,
    ) : AppError

    /** Запрошенный ресурс не найден (HTTP 404). */
    data class NotFound(
        override val cause: Throwable? = null,
    ) : AppError

    /** Всё неожиданное; такие ошибки обязательно логируются. */
    data class Unknown(
        override val cause: Throwable? = null,
    ) : AppError
}
