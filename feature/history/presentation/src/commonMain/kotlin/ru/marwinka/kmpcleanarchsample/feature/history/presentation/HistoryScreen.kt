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
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.resources.history_duration_minutes
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.resources.history_empty

/** Stateful entry point of the history screen. */
@Composable
fun HistoryRoute(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HistoryContent(state = state, modifier = modifier)
}

@Composable
internal fun HistoryContent(
    state: HistoryUiState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is HistoryUiState.Loading ->
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        is HistoryUiState.Content ->
            if (state.entries.isEmpty()) {
                Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.history_empty))
                }
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.entries, key = { it.id }) { entry ->
                        TaskCard(
                            title = entry.title,
                            trailingText = stringResource(Res.string.history_duration_minutes, entry.durationMillis / 60_000),
                        )
                    }
                }
            }
    }
}

@Preview
@Composable
private fun HistoryContentPreview() {
    HistoryContent(
        state =
            HistoryUiState.Content(
                listOf(
                    TaskHistoryEntry(
                        id = "1",
                        title = "Write the architecture doc",
                        completedAtEpochMillis = 0,
                        durationMillis = 1_800_000,
                    ),
                ),
            ),
    )
}
