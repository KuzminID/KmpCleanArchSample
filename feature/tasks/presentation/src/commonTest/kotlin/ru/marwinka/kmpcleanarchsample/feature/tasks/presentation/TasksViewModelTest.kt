package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.marwinka.kmpcleanarchsample.core.AppError
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.CompleteTaskUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.GetActiveTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.RefreshTasksUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeTaskRepository(
    initial: List<Task> = emptyList(),
) : TaskRepository {
    val tasks = MutableStateFlow(initial)
    var refreshGate: CompletableDeferred<AppResult<Unit>> = CompletableDeferred(AppResult.Success(Unit))
    var completedId: String? = null

    override fun observeActive(): Flow<List<Task>> = tasks

    override suspend fun refresh(): AppResult<Unit> = refreshGate.await()

    override suspend fun complete(id: String): AppResult<Unit> {
        completedId = id
        return AppResult.Success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    private val task = Task(id = "1", title = "Write a test", createdAtEpochMillis = 0)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repository: TaskRepository) =
        TasksViewModel(
            getActiveTasks = GetActiveTasksUseCase(repository),
            refreshTasks = RefreshTasksUseCase(repository),
            completeTask = CompleteTaskUseCase(repository),
        )

    @Test
    fun stays_in_initial_loading_until_the_first_refresh_finishes() =
        runTest {
            val repository = FakeTaskRepository().apply { refreshGate = CompletableDeferred() }
            val model = viewModel(repository)
            val states = mutableListOf<TasksUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.collect { states += it } }
            runCurrent()

            assertTrue(states.last().isInitialLoading)

            repository.refreshGate.complete(AppResult.Success(Unit))
            runCurrent()

            assertFalse(states.last().isInitialLoading)
            assertTrue(states.last().tasks.isEmpty())
            assertNull(states.last().error)
        }

    @Test
    fun cached_tasks_are_shown_without_waiting_for_refresh() =
        runTest {
            val repository = FakeTaskRepository(listOf(task)).apply { refreshGate = CompletableDeferred() }
            val model = viewModel(repository)
            val states = mutableListOf<TasksUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.collect { states += it } }
            runCurrent()

            assertEquals(listOf(task), states.last().tasks)
            assertFalse(states.last().isInitialLoading)
        }

    @Test
    fun failed_refresh_is_reported_as_a_typed_error() =
        runTest {
            val repository =
                FakeTaskRepository().apply {
                    refreshGate = CompletableDeferred(AppResult.Failure(AppError.Network()))
                }
            val model = viewModel(repository)
            val states = mutableListOf<TasksUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.collect { states += it } }
            runCurrent()

            assertEquals(TasksUiError.Network, states.last().error)
            assertFalse(states.last().isInitialLoading)
        }

    @Test
    fun retry_clears_the_error_after_a_successful_refresh() =
        runTest {
            val repository =
                FakeTaskRepository().apply {
                    refreshGate = CompletableDeferred(AppResult.Failure(AppError.Unknown()))
                }
            val model = viewModel(repository)
            val states = mutableListOf<TasksUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.state.collect { states += it } }
            runCurrent()
            assertEquals(TasksUiError.Unknown, states.last().error)

            repository.refreshGate = CompletableDeferred(AppResult.Success(Unit))
            model.refresh()
            runCurrent()

            assertNull(states.last().error)
        }

    @Test
    fun completing_a_task_delegates_to_the_repository() =
        runTest {
            val repository = FakeTaskRepository(listOf(task))
            val model = viewModel(repository)

            model.onTaskDone("1")
            runCurrent()

            assertEquals("1", repository.completedId)
        }
}
