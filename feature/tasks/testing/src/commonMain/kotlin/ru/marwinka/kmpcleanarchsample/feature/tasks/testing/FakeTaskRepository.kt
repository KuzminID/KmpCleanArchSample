package ru.marwinka.kmpcleanarchsample.feature.tasks.testing

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository

/**
 * Единственный fake репозитория задач для тестов domain и presentation. Рукописный, без
 * mock-фреймворка: он одинаково работает на JVM и в общем коде KMP.
 */
class FakeTaskRepository(
    initial: List<Task> = emptyList(),
) : TaskRepository {
    /** Содержимое «хранилища»: тест меняет его, чтобы сымитировать эмиссию из базы. */
    val activeTasks = MutableStateFlow(initial)

    /** Результат следующего `refresh`; незавершённый `CompletableDeferred` держит обновление в процессе. */
    var refreshResult: CompletableDeferred<AppResult<Unit>> = CompletableDeferred(AppResult.Success(Unit))

    var completeResult: AppResult<Unit> = AppResult.Success(Unit)

    var refreshCalls: Int = 0
        private set

    private val _completedIds = mutableListOf<String>()
    val completedIds: List<String> get() = _completedIds

    override fun observeActive(): Flow<List<Task>> = activeTasks

    override suspend fun refresh(): AppResult<Unit> {
        refreshCalls++
        return refreshResult.await()
    }

    override suspend fun complete(id: String): AppResult<Unit> {
        _completedIds += id
        return completeResult
    }
}
