package ru.marwinka.kmpcleanarchsample.feature.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.marwinka.kmpcleanarchsample.designsystem.TaskCard
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.resources.Res
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.resources.history_empty

/** Точка входа экрана истории со состоянием. */
@Composable
fun HistoryRoute(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(uiState = uiState, modifier = modifier)
}

/** Экран истории без состояния. */
@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading ->
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        uiState.entries.isEmpty() ->
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.history_empty))
            }

        else ->
            LazyColumn(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.entries, key = { it.id }) { entry ->
                    TaskCard(title = entry.title)
                }
            }
    }
}

@Preview
@Composable
private fun HistoryScreenLoadingPreview() = HistoryScreen(HistoryUiState())

@Preview
@Composable
private fun HistoryScreenEmptyPreview() = HistoryScreen(HistoryUiState(isLoading = false))

@Preview
@Composable
private fun HistoryScreenContentPreview() =
    HistoryScreen(
        HistoryUiState(
            entries = listOf(TaskHistoryEntry(id = "1", title = "S01E01 · Pilot", completedAtEpochMillis = 0)),
            isLoading = false,
        ),
    )
