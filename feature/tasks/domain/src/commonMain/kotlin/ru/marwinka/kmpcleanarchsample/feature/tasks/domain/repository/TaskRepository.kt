package ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task

interface TaskRepository {
    fun observeActive(): Flow<List<Task>>

    suspend fun refresh()

    suspend fun complete(id: String)
}
