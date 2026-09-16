package ru.marwinka.kmpcleanarchsample.core.network

import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> { createHttpClient() }
}
