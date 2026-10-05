package ru.marwinka.kmpcleanarchsample.feature.tasks.api

import kotlinx.coroutines.flow.Flow

/**
 * Контракт фичи tasks для других фич: выполненные задачи без привязки к способу хранения.
 * Другие фичи зависят только от этого модуля, а не от `feature:tasks:data`.
 */
interface CompletedTasksSource {
    fun observeCompleted(): Flow<List<CompletedTask>>
}

data class CompletedTask(
    val id: String,
    val title: String,
    val completedAtEpochMillis: Long,
)
