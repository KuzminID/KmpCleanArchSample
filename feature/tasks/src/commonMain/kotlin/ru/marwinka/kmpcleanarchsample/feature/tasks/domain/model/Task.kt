package ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model

data class Task(
    val id: String,
    val title: String,
    val createdAtEpochMillis: Long,
)
