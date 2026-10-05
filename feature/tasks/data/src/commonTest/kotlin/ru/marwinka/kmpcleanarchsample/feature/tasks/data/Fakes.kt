package ru.marwinka.kmpcleanarchsample.feature.tasks.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.CompletedTaskRow
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskCompletionEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskDao
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.EpisodeDto
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.TaskApi

/** Fake DAO для тестов репозитория; настоящие запросы проверяются в jvmTest на Room в памяти. */
internal class FakeTaskDao(
    initial: List<TaskEntity> = emptyList(),
) : TaskDao {
    val tasks = MutableStateFlow(initial)
    val completions = mutableListOf<TaskCompletionEntity>()
    var failure: Throwable? = null

    override fun observeActive(): Flow<List<TaskEntity>> = tasks

    override fun observeCompleted(): Flow<List<CompletedTaskRow>> = MutableStateFlow(emptyList())

    override suspend fun replaceAll(tasks: List<TaskEntity>) {
        failure?.let { throw it }
        this.tasks.value = tasks
    }

    override suspend fun deleteAll() {
        tasks.value = emptyList()
    }

    override suspend fun insertAll(tasks: List<TaskEntity>) {
        this.tasks.value += tasks
    }

    override suspend fun insertCompletion(completion: TaskCompletionEntity) {
        failure?.let { throw it }
        completions += completion
    }
}

internal class FakeTaskApi(
    var result: CompletableDeferred<AppResult<List<EpisodeDto>>> = CompletableDeferred(AppResult.Success(emptyList())),
) : TaskApi {
    override suspend fun fetchEpisodes(): AppResult<List<EpisodeDto>> = result.await()
}
