package ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * A hand-written fake, not a mocking framework — the whole point of the
 * repository/use-case split is that this is all a use-case test needs.
 *
 * RU: Написанный вручную фейк, а не мок-фреймворк — в этом весь смысл разделения
 * repository/use-case: тесту use-case больше ничего и не требуется.
 */
private class FakeTaskRepository(
    private val tasks: List<Task>,
) : TaskRepository {
    var completedId: String? = null
        private set

    override fun observeActive(): Flow<List<Task>> = flowOf(tasks)

    override suspend fun refresh() = Unit

    override suspend fun complete(id: String) {
        completedId = id
    }
}

class TaskUseCasesTest {
    private val tasks = listOf(Task(id = "1", title = "Write a test", createdAtEpochMillis = 0))

    @Test
    fun getActiveTasks_returns_tasks_from_repository() =
        runBlocking {
            val useCase = GetActiveTasksUseCase(FakeTaskRepository(tasks))

            assertEquals(tasks, useCase().first())
        }

    @Test
    fun toggleTaskDone_delegates_to_repository() =
        runBlocking {
            val repository = FakeTaskRepository(tasks)
            assertNull(repository.completedId)

            ToggleTaskDoneUseCase(repository)("1")

            assertEquals("1", repository.completedId)
        }
}
