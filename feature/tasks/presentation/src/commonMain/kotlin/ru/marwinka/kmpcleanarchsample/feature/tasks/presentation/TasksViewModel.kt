package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.marwinka.kmpcleanarchsample.core.AppError
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.CompleteTaskUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.GetActiveTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.RefreshTasksUseCase

/** Тип ошибки для UI; текст подбирает экран, а не модель. */
sealed interface TasksUiError {
    data object Network : TasksUiError

    data object NotFound : TasksUiError

    data object Unknown : TasksUiError
}

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    /** Данных ещё не было: в базе пусто и первое обновление не завершилось. */
    val isInitialLoading: Boolean = true,
    /** Идёт обновление поверх уже показанных данных. */
    val isRefreshing: Boolean = false,
    val error: TasksUiError? = null,
)

class TasksViewModel(
    getActiveTasks: GetActiveTasksUseCase,
    private val refreshTasks: RefreshTasksUseCase,
    private val completeTask: CompleteTaskUseCase,
) : ViewModel() {
    private data class RefreshState(
        val isRunning: Boolean = false,
        val hasFinished: Boolean = false,
        val error: TasksUiError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())
    private var refreshJob: Job? = null

    val uiState: StateFlow<TasksUiState> =
        combine(getActiveTasks(), refreshState) { tasks, refresh ->
            val isInitialLoading = tasks.isEmpty() && !refresh.hasFinished
            TasksUiState(
                tasks = tasks,
                isInitialLoading = isInitialLoading,
                isRefreshing = refresh.isRunning && !isInitialLoading,
                error = refresh.error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = TasksUiState(),
        )

    init {
        onRefresh()
    }

    /** Повторный вызов во время идущего обновления игнорируется: два запроса не гоняются друг с другом. */
    fun onRefresh() {
        if (refreshJob?.isActive == true) return
        refreshJob =
            viewModelScope.launch {
                refreshState.update { it.copy(isRunning = true, error = null) }
                val error = (refreshTasks() as? AppResult.Failure)?.error?.toUiError()
                refreshState.value = RefreshState(hasFinished = true, error = error)
            }
    }

    fun onTaskDone(id: String) {
        viewModelScope.launch {
            val failure = completeTask(id) as? AppResult.Failure ?: return@launch
            refreshState.update { it.copy(error = failure.error.toUiError()) }
        }
    }

    private fun AppError.toUiError(): TasksUiError =
        when (this) {
            is AppError.Network -> TasksUiError.Network
            is AppError.NotFound -> TasksUiError.NotFound
            is AppError.Unknown -> TasksUiError.Unknown
        }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
