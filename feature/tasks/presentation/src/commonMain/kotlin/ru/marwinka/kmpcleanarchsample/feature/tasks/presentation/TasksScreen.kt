package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ru.marwinka.kmpcleanarchsample.designsystem.RefreshableContent
import ru.marwinka.kmpcleanarchsample.designsystem.TaskCard
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.Res
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.tasks_empty
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.tasks_error_network
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.tasks_error_not_found
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.tasks_error_unknown
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.resources.tasks_retry

/** Точка входа экрана со состоянием: получает ViewModel и собирает её состояние. */
@Composable
fun TasksRoute(
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TasksScreen(
        uiState = uiState,
        onTaskDone = viewModel::onTaskDone,
        onTaskClick = onTaskClick,
        onRefresh = viewModel::onRefresh,
        modifier = modifier,
    )
}

/** Экран без состояния: ничего не знает о ViewModel и DI, поэтому его можно показать в превью. */
@Composable
internal fun TasksScreen(
    uiState: TasksUiState,
    onTaskDone: (String) -> Unit,
    onTaskClick: (Task) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isInitialLoading ->
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        uiState.error != null && uiState.tasks.isEmpty() ->
            Column(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(uiState.error.messageRes()))
                if (uiState.isRefreshing) {
                    CircularProgressIndicator()
                } else {
                    Button(onClick = onRefresh) { Text(stringResource(Res.string.tasks_retry)) }
                }
            }

        else ->
            RefreshableContent(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (uiState.error != null) {
                        item { ErrorBanner(error = uiState.error, onRetry = onRefresh) }
                    }
                    if (uiState.tasks.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(Res.string.tasks_empty),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            )
                        }
                    }
                    items(uiState.tasks, key = { it.id }) { task ->
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
}

/** Ошибка обновления поверх уже показанных данных: список не пропадает. */
@Composable
private fun ErrorBanner(
    error: TasksUiError,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(error.messageRes()),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(onClick = onRetry) { Text(stringResource(Res.string.tasks_retry)) }
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
        Task(id = "1", title = "S01E01 · Pilot"),
        Task(id = "2", title = "S01E02 · Lawnmower Dog"),
    )

@Composable
private fun TasksScreenPreview(uiState: TasksUiState) {
    TasksScreen(uiState = uiState, onTaskDone = {}, onTaskClick = {}, onRefresh = {})
}

@Preview
@Composable
private fun TasksScreenLoadingPreview() = TasksScreenPreview(TasksUiState())

@Preview
@Composable
private fun TasksScreenContentPreview() = TasksScreenPreview(TasksUiState(tasks = previewTasks, isInitialLoading = false))

@Preview
@Composable
private fun TasksScreenRefreshingPreview() =
    TasksScreenPreview(TasksUiState(tasks = previewTasks, isInitialLoading = false, isRefreshing = true))

@Preview
@Composable
private fun TasksScreenEmptyPreview() = TasksScreenPreview(TasksUiState(isInitialLoading = false))

@Preview
@Composable
private fun TasksScreenErrorWithDataPreview() =
    TasksScreenPreview(TasksUiState(tasks = previewTasks, isInitialLoading = false, error = TasksUiError.Network))

@Preview
@Composable
private fun TasksScreenErrorPreview() = TasksScreenPreview(TasksUiState(isInitialLoading = false, error = TasksUiError.Network))
