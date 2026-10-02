package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.marwinka.kmpcleanarchsample.designsystem.TaskCard
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.Res
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.tasks_error_network
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.tasks_error_not_found
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.tasks_error_unknown
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.tasks_retry

/** Stateful entry point of the task list: resolves the ViewModel and collects its state. */
@Composable
fun TasksRoute(
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TasksContent(
        state = state,
        onTaskDone = viewModel::onTaskDone,
        onTaskClick = onTaskClick,
        onRetry = viewModel::refresh,
        modifier = modifier,
    )
}

/** Stateless task list: knows nothing about the ViewModel or DI, so it can be previewed and tested. */
@Composable
internal fun TasksContent(
    state: TasksUiState,
    onTaskDone: (String) -> Unit,
    onTaskClick: (Task) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isInitialLoading ->
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        state.error != null && state.tasks.isEmpty() ->
            Column(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(state.error.messageRes()))
                Button(onClick = onRetry) { Text(stringResource(Res.string.tasks_retry)) }
            }

        else ->
            LazyColumn(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.tasks, key = { it.id }) { task ->
                    TaskCard(
                        title = task.title,
                        checked = false,
                        onCheckedChange = { onTaskDone(task.id) },
                        onClick = { onTaskClick(task) },
                    )
                }
            }
    }
}

private fun TasksUiError.messageRes(): StringResource =
    when (this) {
        TasksUiError.Network -> Res.string.tasks_error_network
        TasksUiError.NotFound -> Res.string.tasks_error_not_found
        TasksUiError.Unknown -> Res.string.tasks_error_unknown
    }

private val previewTasks =
    listOf(
        Task(id = "1", title = "Write the architecture doc", createdAtEpochMillis = 0),
        Task(id = "2", title = "Migrate to Navigation 3", createdAtEpochMillis = 0),
    )

@Preview
@Composable
private fun TasksContentPreview() {
    TasksContent(state = TasksUiState(tasks = previewTasks, isInitialLoading = false), onTaskDone = {}, onTaskClick = {}, onRetry = {})
}

@Preview
@Composable
private fun TasksContentErrorPreview() {
    TasksContent(
        state = TasksUiState(isInitialLoading = false, error = TasksUiError.Network),
        onTaskDone = {},
        onTaskClick = {},
        onRetry = {},
    )
}
