package ru.marwinka.kmpcleanarchsample.feature.tasks.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.core.DispatcherProvider
import ru.marwinka.kmpcleanarchsample.core.Logger
import ru.marwinka.kmpcleanarchsample.core.appResultOf
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskCompletionEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskDao
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.TaskApi
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository
import kotlin.time.Clock

/**
 * Offline-first: UI читает задачи только из базы, `refresh` наполняет её с сервера.
 * Стратегия изменений — «сначала хранилище»: отметка о выполнении пишется в локальную
 * таблицу, очереди синхронизации нет, потому что API только на чтение.
 */
internal class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val api: TaskApi,
    private val clock: Clock,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
) : TaskRepository {
    override fun observeActive(): Flow<List<Task>> =
        dao
            .observeActive()
            .map { rows -> rows.map(TaskEntity::toDomain) }
            .flowOn(dispatchers.default)

    override suspend fun refresh(): AppResult<Unit> =
        when (val fetched = api.fetchEpisodes()) {
            is AppResult.Failure -> fetched
            is AppResult.Success ->
                appResultOf(logger, dispatchers.io) {
                    dao.replaceAll(fetched.value.mapIndexed { index, dto -> dto.toEntity(position = index) })
                }
        }

    override suspend fun complete(id: String): AppResult<Unit> =
        appResultOf(logger, dispatchers.io) {
            dao.insertCompletion(TaskCompletionEntity(taskId = id, completedAt = clock.now().toEpochMilliseconds()))
        }
}
