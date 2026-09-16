package ru.marwinka.kmpcleanarchsample.feature.history.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.domain.usecase.GetTaskHistoryUseCase

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Content(val entries: List<TaskHistoryEntry>) : HistoryUiState
}

class HistoryScreenModel(
    private val getTaskHistory: GetTaskHistoryUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        screenModelScope.launch {
            getTaskHistory().collect { entries -> _state.value = HistoryUiState.Content(entries) }
        }
    }
}
