package ru.marwinka.kmpcleanarchsample.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 Базовый HTTP-клиент, общий для всех удалённых источников данных. Ничего
 не знает о конкретных эндпоинтах — это забота источника данных, который его использует.
 */
fun createHttpClient(): HttpClient = HttpClient {
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }

    install(Logging) {
        level = LogLevel.INFO
    }
}
