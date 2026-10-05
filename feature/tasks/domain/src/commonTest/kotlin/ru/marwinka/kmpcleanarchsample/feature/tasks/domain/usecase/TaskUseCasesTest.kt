package ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.testing.FakeTaskRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class TaskUseCasesTest {
    private val tasks = listOf(Task(id = "1", title = "S01E01 · Pilot"))

    @Test
    fun getActiveTasks_returns_tasks_from_repository() =
        runTest {
            val useCase = GetActiveTasksUseCase(FakeTaskRepository(tasks))

            assertEquals(tasks, useCase().first())
        }

    @Test
    fun completeTask_delegates_to_repository() =
        runTest {
            val repository = FakeTaskRepository(tasks)

            val result = CompleteTaskUseCase(repository)("1")

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(listOf("1"), repository.completedIds)
        }

    @Test
    fun refreshTasks_delegates_to_repository() =
        runTest {
            val repository = FakeTaskRepository()

            RefreshTasksUseCase(repository)()

            assertEquals(1, repository.refreshCalls)
        }
}
