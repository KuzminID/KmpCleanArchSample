package ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote

import kotlinx.serialization.Serializable

/** Страница ответа `GET /episode` Rick and Morty API. */
@Serializable
internal data class EpisodePageDto(
    val info: PageInfoDto,
    val results: List<EpisodeDto>,
)

@Serializable
internal data class PageInfoDto(
    val next: String?,
)

@Serializable
internal data class EpisodeDto(
    val id: Int,
    val name: String,
    /** Код эпизода вида `S01E01`. */
    val episode: String,
)
