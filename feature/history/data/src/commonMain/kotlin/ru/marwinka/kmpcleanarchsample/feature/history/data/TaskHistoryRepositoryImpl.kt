package ru.marwinka.kmpcleanarchsample.feature.history.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTask
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTasksSource

class TaskHistoryRepositoryImpl(
    private val completedTasks: CompletedTasksSource,
) : TaskHistoryRepository {
    override fun observeHistory(): Flow<List<TaskHistoryEntry>> =
        completedTasks.observeCompleted().map { tasks ->
            tasks.map { it.toEntry() }
        }

    private fun CompletedTask.toEntry() =
        TaskHistoryEntry(
            id = id,
            title = title,
            completedAtEpochMillis = completedAtEpochMillis,
            durationMillis = completedAtEpochMillis - createdAtEpochMillis,
        )
}
