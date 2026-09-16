package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.marwinka.kmpcleanarchsample.designsystem.TaskCard

/** Экран списка активных задач — верхний экран стека [TasksTab]. */
data object TasksScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<TasksScreenModel>()
        val navigator = LocalNavigator.currentOrThrow
        val state by screenModel.state.collectAsState()

        when (val current = state) {
            is TasksUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            is TasksUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(current.message)
            }

            is TasksUiState.Content -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(current.tasks, key = { it.id }) { task ->
                    TaskCard(
                        title = task.title,
                        checked = false,
                        onCheckedChange = { screenModel.onTaskDone(task.id) },
                        onClick = { navigator.push(TaskDetailsScreen(taskId = task.id, title = task.title)) },
                    )
                }
            }
        }
    }
}
