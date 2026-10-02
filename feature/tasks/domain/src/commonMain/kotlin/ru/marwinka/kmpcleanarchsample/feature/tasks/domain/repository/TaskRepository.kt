package ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task

interface TaskRepository {
    fun observeActive(): Flow<List<Task>>

    suspend fun refresh(): AppResult<Unit>

    suspend fun complete(id: String): AppResult<Unit>
}
