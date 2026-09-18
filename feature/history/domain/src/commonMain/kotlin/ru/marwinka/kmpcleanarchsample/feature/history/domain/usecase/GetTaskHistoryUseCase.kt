package ru.marwinka.kmpcleanarchsample.feature.history.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository

class GetTaskHistoryUseCase(
    private val repository: TaskHistoryRepository,
) {
    operator fun invoke(): Flow<List<TaskHistoryEntry>> = repository.observeHistory()
}
