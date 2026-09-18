package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.marwinka.kmpcleanarchsample.core.resultOf
import ru.marwinka.kmpcleanarchsample.core.toAppError
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.GetActiveTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.RefreshTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.ToggleTaskDoneUseCase

sealed interface TasksUiState {
    data object Loading : TasksUiState

    data class Content(
        val tasks: List<Task>,
    ) : TasksUiState

    data class Error(
        val message: String,
    ) : TasksUiState
}

class TasksScreenModel(
    private val getActiveTasks: GetActiveTasksUseCase,
    private val refreshTasks: RefreshTasksUseCase,
    private val toggleTaskDone: ToggleTaskDoneUseCase,
) : ScreenModel {
    private val _state = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    init {
        screenModelScope.launch {
            resultOf { refreshTasks() }.onFailure { _state.value = TasksUiState.Error(it.toAppError().message) }
        }
        screenModelScope.launch {
            getActiveTasks().collect { tasks -> _state.value = TasksUiState.Content(tasks) }
        }
    }

    fun onTaskDone(id: String) {
        screenModelScope.launch { toggleTaskDone(id) }
    }
}
