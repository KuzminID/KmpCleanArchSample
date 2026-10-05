package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.marwinka.kmpcleanarchsample.core.AppError
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.CompleteTaskUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.GetActiveTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.RefreshTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.testing.FakeTaskRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    private val task = Task(id = "1", title = "S01E01 · Pilot")

    // runTest берёт планировщик подменённого Main, поэтому корутины ViewModel и теста идут в одном времени.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repository: FakeTaskRepository) =
        TasksViewModel(
            getActiveTasks = GetActiveTasksUseCase(repository),
            refreshTasks = RefreshTasksUseCase(repository),
            completeTask = CompleteTaskUseCase(repository),
        )

    /** Подписывается на состояние, как это делает экран, и возвращает последнее значение. */
    private fun TestScope.collect(model: TasksViewModel): () -> TasksUiState {
        val states = mutableListOf<TasksUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.uiState.collect { states += it } }
        runCurrent()
        return { states.last() }
    }

    @Test
    fun empty_storage_does_not_end_initial_loading_before_the_first_refresh_finishes() =
        runTest {
            val repository = FakeTaskRepository().apply { refreshResult = CompletableDeferred() }
            val state = collect(viewModel(repository))

            assertTrue(state().isInitialLoading)

            repository.refreshResult.complete(AppResult.Success(Unit))
            runCurrent()

            assertFalse(state().isInitialLoading)
            assertTrue(state().tasks.isEmpty())
            assertNull(state().error)
        }

    @Test
    fun cached_tasks_are_shown_without_waiting_for_refresh() =
        runTest {
            val repository = FakeTaskRepository(listOf(task)).apply { refreshResult = CompletableDeferred() }
            val state = collect(viewModel(repository))

            assertEquals(listOf(task), state().tasks)
            assertFalse(state().isInitialLoading)
            assertTrue(state().isRefreshing)
        }

    @Test
    fun failed_refresh_keeps_cached_tasks_and_reports_a_typed_error() =
        runTest {
            val repository =
                FakeTaskRepository(listOf(task)).apply {
                    refreshResult = CompletableDeferred(AppResult.Failure(AppError.Network()))
                }
            val state = collect(viewModel(repository))

            assertEquals(listOf(task), state().tasks)
            assertEquals(TasksUiError.Network, state().error)
            assertFalse(state().isRefreshing)
        }

    @Test
    fun retry_clears_the_error_after_a_successful_refresh() =
        runTest {
            val repository =
                FakeTaskRepository().apply {
                    refreshResult = CompletableDeferred(AppResult.Failure(AppError.Unknown()))
                }
            val model = viewModel(repository)
            val state = collect(model)
            assertEquals(TasksUiError.Unknown, state().error)

            repository.refreshResult = CompletableDeferred()
            model.onRefresh()
            runCurrent()
            assertNull(state().error)
            assertFalse(state().isInitialLoading)

            repository.refreshResult.complete(AppResult.Success(Unit))
            runCurrent()
            assertNull(state().error)
        }

    @Test
    fun user_refresh_over_shown_data_is_refreshing_not_initial_loading() =
        runTest {
            val repository = FakeTaskRepository(listOf(task))
            val model = viewModel(repository)
            val state = collect(model)

            repository.refreshResult = CompletableDeferred()
            model.onRefresh()
            runCurrent()

            assertTrue(state().isRefreshing)
            assertFalse(state().isInitialLoading)
        }

    @Test
    fun refresh_requested_while_another_is_running_is_ignored() =
        runTest {
            val repository = FakeTaskRepository(listOf(task)).apply { refreshResult = CompletableDeferred() }
            val model = viewModel(repository)
            collect(model)

            model.onRefresh()
            model.onRefresh()
            runCurrent()

            assertEquals(1, repository.refreshCalls)
        }

    @Test
    fun completing_a_task_delegates_to_the_repository() =
        runTest {
            val repository = FakeTaskRepository(listOf(task))
            val model = viewModel(repository)

            model.onTaskDone("1")
            runCurrent()

            assertEquals(listOf("1"), repository.completedIds)
        }

    @Test
    fun failed_completion_is_reported_as_a_typed_error() =
        runTest {
            val repository = FakeTaskRepository(listOf(task)).apply { completeResult = AppResult.Failure(AppError.Unknown()) }
            val model = viewModel(repository)
            val state = collect(model)

            model.onTaskDone("1")
            runCurrent()

            assertEquals(TasksUiError.Unknown, state().error)
        }
}
