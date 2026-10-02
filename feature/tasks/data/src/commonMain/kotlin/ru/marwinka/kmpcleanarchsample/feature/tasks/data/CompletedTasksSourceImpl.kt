package ru.marwinka.kmpcleanarchsample.feature.tasks.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.marwinka.kmpcleanarchsample.core.DispatcherProvider
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTask
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTasksSource
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskDao
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskEntity

class CompletedTasksSourceImpl(
    private val dao: TaskDao,
    private val dispatchers: DispatcherProvider,
) : CompletedTasksSource {
    override fun observeCompleted(): Flow<List<CompletedTask>> =
        dao
            .observeDone()
            .map { rows -> rows.map { it.toCompleted() } }
            .flowOn(dispatchers.default)

    private fun TaskEntity.toCompleted() =
        CompletedTask(
            id = id,
            title = title,
            createdAtEpochMillis = createdAt,
            completedAtEpochMillis = completedAt ?: createdAt,
        )
}
