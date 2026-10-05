package ru.marwinka.kmpcleanarchsample.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.marwinka.kmpcleanarchsample.core.Logger
import io.ktor.client.plugins.logging.Logger as KtorLogger

/** Базовый адрес бэкенда приложения — Rick and Morty API. */
const val BASE_URL = "https://rickandmortyapi.com/api/"

/**
 * Базовый HTTP-клиент, общий для всех удалённых источников данных. Ничего не знает
 * о конкретных эндпоинтах: пути запросов задаёт источник данных фичи.
 * Движок выбирается по платформе: OkHttp на Android и JVM, Darwin на iOS.
 */
fun createHttpClient(
    logger: Logger,
    baseUrl: String = BASE_URL,
): HttpClient = HttpClient { configure(logger, baseUrl) }

/** Тот же клиент поверх заданного движка; в тестах — `MockEngine`. */
fun createHttpClient(
    engine: HttpClientEngine,
    logger: Logger,
    baseUrl: String = BASE_URL,
): HttpClient = HttpClient(engine) { configure(logger, baseUrl) }

private fun HttpClientConfig<*>.configure(
    logger: Logger,
    baseUrl: String,
) {
    // Ответы 3xx–5xx превращаются в исключения, которые networkResultOf типизирует в AppError.
    expectSuccess = true

    defaultRequest { url(baseUrl) }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    install(Logging) {
        level = LogLevel.INFO
        this.logger =
            object : KtorLogger {
                override fun log(message: String) = logger.debug(TAG, message)
            }
    }
}

private const val TAG = "Http"
