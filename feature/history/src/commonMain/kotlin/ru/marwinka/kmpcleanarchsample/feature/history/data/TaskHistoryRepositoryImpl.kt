package ru.marwinka.kmpcleanarchsample.feature.history.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.marwinka.kmpcleanarchsample.data.tasks.TaskDao
import ru.marwinka.kmpcleanarchsample.data.tasks.TaskEntity
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository

/**
 * Репозиторий работает с тем же dto, что и feature/tasks, но маппит в свою модель данных
 */
internal class TaskHistoryRepositoryImpl(
    private val dao: TaskDao,
) : TaskHistoryRepository {

    override fun observeHistory(): Flow<List<TaskHistoryEntry>> =
        dao.observeDone().map { rows -> rows.map { it.toDomain() } }

    private fun TaskEntity.toDomain() = TaskHistoryEntry(
        id = id,
        title = title,
        completedAtEpochMillis = completedAt ?: createdAt,
        durationMillis = (completedAt ?: createdAt) - createdAt,
    )
}
