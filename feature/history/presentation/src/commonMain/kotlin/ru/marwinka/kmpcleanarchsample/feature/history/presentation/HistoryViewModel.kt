package ru.marwinka.kmpcleanarchsample.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.domain.usecase.GetTaskHistoryUseCase

data class HistoryUiState(
    val entries: List<TaskHistoryEntry> = emptyList(),
    /** Хранилище ещё не отдало первую эмиссию. */
    val isLoading: Boolean = true,
)

class HistoryViewModel(
    getTaskHistory: GetTaskHistoryUseCase,
) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> =
        getTaskHistory()
            .map { entries -> HistoryUiState(entries = entries, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = HistoryUiState(),
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
