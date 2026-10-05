package ru.marwinka.kmpcleanarchsample.feature.history.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.marwinka.kmpcleanarchsample.core.DispatcherProvider
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTask
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTasksSource

/** История строится из контракта фичи tasks: таблицы и DAO другой фичи отсюда не видны. */
internal class TaskHistoryRepositoryImpl(
    private val completedTasks: CompletedTasksSource,
    private val dispatchers: DispatcherProvider,
) : TaskHistoryRepository {
    override fun observeHistory(): Flow<List<TaskHistoryEntry>> =
        completedTasks
            .observeCompleted()
            .map { tasks -> tasks.map(CompletedTask::toHistoryEntry) }
            .flowOn(dispatchers.default)
}
