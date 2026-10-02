package ru.marwinka.kmpcleanarchsample.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.domain.usecase.GetTaskHistoryUseCase

sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Content(
        val entries: List<TaskHistoryEntry>,
    ) : HistoryUiState
}

class HistoryViewModel(
    getTaskHistory: GetTaskHistoryUseCase,
) : ViewModel() {
    val state: StateFlow<HistoryUiState> =
        getTaskHistory()
            .map<_, HistoryUiState> { entries -> HistoryUiState.Content(entries) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = HistoryUiState.Loading,
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
