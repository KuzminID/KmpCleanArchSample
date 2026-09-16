package ru.marwinka.kmpcleanarchsample.feature.history.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry

interface TaskHistoryRepository {
    fun observeHistory(): Flow<List<TaskHistoryEntry>>
}
