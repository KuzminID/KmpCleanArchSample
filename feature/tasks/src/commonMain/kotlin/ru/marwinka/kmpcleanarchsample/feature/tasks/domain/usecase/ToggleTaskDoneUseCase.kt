package ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase

import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository

class ToggleTaskDoneUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(id: String) = repository.complete(id)
}
