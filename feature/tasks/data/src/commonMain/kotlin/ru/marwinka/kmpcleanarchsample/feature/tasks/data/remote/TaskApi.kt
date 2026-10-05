package ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.core.Logger
import ru.marwinka.kmpcleanarchsample.core.network.networkResultOf

/** Удалённый источник задач фичи. Ошибки типизирует `core:network`. */
internal interface TaskApi {
    suspend fun fetchEpisodes(): AppResult<List<EpisodeDto>>
}

internal class KtorTaskApi(
    private val client: HttpClient,
    private val logger: Logger,
) : TaskApi {
    override suspend fun fetchEpisodes(): AppResult<List<EpisodeDto>> =
        networkResultOf(logger) {
            buildList {
                var page = 1
                do {
                    val response = client.get("episode") { parameter("page", page) }.body<EpisodePageDto>()
                    addAll(response.results)
                    page++
                } while (response.info.next != null && page <= MAX_PAGES)
            }
        }

    private companion object {
        /** Защита от бесконечного цикла, если сервер всегда отдаёт `next`; эпизодов сейчас 3 страницы. */
        const val MAX_PAGES = 20
    }
}
