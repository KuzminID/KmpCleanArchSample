package ru.marwinka.kmpcleanarchsample.feature.tasks.data

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.marwinka.kmpcleanarchsample.data.tasks.TaskApi
import ru.marwinka.kmpcleanarchsample.data.tasks.TaskDao
import ru.marwinka.kmpcleanarchsample.data.tasks.TaskEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository

@OptIn(ExperimentalTime::class)
internal class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val api: TaskApi,
) : TaskRepository {

    override fun observeActive(): Flow<List<Task>> =
        dao.observeActive().map { rows -> rows.map { it.toDomain() } }

    override suspend fun refresh() {
        if (dao.count() > 0) return
        val now = Clock.System.now().toEpochMilliseconds()
        val seeded = api.fetchTasks().map { dto ->
            TaskEntity(id = dto.id, title = dto.title, status = STATUS_ACTIVE, createdAt = now, completedAt = null)
        }
        dao.upsertAll(seeded)
    }

    override suspend fun complete(id: String) {
        dao.markDone(id, Clock.System.now().toEpochMilliseconds())
    }

    private fun TaskEntity.toDomain() = Task(id = id, title = title, createdAtEpochMillis = createdAt)

    private companion object {
        const val STATUS_ACTIVE = "ACTIVE"
    }
}
