package ru.marwinka.kmpcleanarchsample.feature.history.domain.model

data class TaskHistoryEntry(
    val id: String,
    val title: String,
    val completedAtEpochMillis: Long,
    val durationMillis: Long,
)
