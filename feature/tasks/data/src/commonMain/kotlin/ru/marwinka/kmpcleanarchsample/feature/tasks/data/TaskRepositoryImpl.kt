package ru.marwinka.kmpcleanarchsample.feature.tasks.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.core.DispatcherProvider
import ru.marwinka.kmpcleanarchsample.core.appResultOf
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskDao
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskStatus
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.TaskApi
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val api: TaskApi,
    private val clock: Clock,
    private val dispatchers: DispatcherProvider,
) : TaskRepository {
    override fun observeActive(): Flow<List<Task>> =
        dao
            .observeActive()
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(dispatchers.default)

    override suspend fun refresh(): AppResult<Unit> =
        appResultOf {
            val now = clock.now().toEpochMilliseconds()
            val fetched =
                api.fetchTasks().map { dto ->
                    TaskEntity(id = dto.id, title = dto.title, status = TaskStatus.ACTIVE, createdAt = now, completedAt = null)
                }
            dao.insertNew(fetched)
        }

    override suspend fun complete(id: String): AppResult<Unit> =
        appResultOf {
            dao.markDone(id, clock.now().toEpochMilliseconds())
        }

    private fun TaskEntity.toDomain() = Task(id = id, title = title, createdAtEpochMillis = createdAt)
}
