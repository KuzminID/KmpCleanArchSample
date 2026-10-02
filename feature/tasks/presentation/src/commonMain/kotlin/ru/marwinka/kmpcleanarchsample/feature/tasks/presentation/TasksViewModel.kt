package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    /** Данных ещё не было: в базе пусто и первый refresh не завершился. */
    val isInitialLoading: Boolean = true,
    val error: TasksUiError? = null,
)

class TasksViewModel(
    getActiveTasks: GetActiveTasksUseCase,
    private val refreshTasks: RefreshTasksUseCase,
    private val completeTask: CompleteTaskUseCase,
) : ViewModel() {
    private data class RefreshState(
        val isFinished: Boolean = false,
        val error: TasksUiError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())

    val state: StateFlow<TasksUiState> =
        combine(getActiveTasks(), refreshState) { tasks, refresh ->
            TasksUiState(
                tasks = tasks,
                isInitialLoading = tasks.isEmpty() && !refresh.isFinished,
                error = refresh.error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = TasksUiState(),
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshState.value = RefreshState()
            val error = (refreshTasks() as? AppResult.Failure)?.error?.toUiError()
            refreshState.value = RefreshState(isFinished = true, error = error)
        }
    }

    fun onTaskDone(id: String) {
        viewModelScope.launch { completeTask(id) }
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
