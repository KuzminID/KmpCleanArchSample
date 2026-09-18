package ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository

class GetActiveTasksUseCase(
    private val repository: TaskRepository,
) {
    operator fun invoke(): Flow<List<Task>> = repository.observeActive()
}
