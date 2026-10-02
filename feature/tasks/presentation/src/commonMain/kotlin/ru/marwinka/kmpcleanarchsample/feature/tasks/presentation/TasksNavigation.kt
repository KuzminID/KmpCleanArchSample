package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/** Destinations of the tasks tab; arguments are properties, so navigation is type-safe. */
sealed interface TasksDestination {
    data object List : TasksDestination

    data class Details(
        val taskId: String,
        val title: String,
    ) : TasksDestination
}

/** Initial back stack of the tab; the app owns it so it survives switching between tabs. */
fun tasksStartDestination(): TasksDestination = TasksDestination.List

/**
 * The tasks tab. Each back-stack entry gets its own ViewModelStore, so a ViewModel lives exactly
 * as long as its screen is on the stack.
 */
@Composable
fun TasksTab(
    backStack: SnapshotStateList<TasksDestination>,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<TasksDestination.List> {
                    TasksRoute(onTaskClick = { task -> backStack.add(TasksDestination.Details(task.id, task.title)) })
                }
                entry<TasksDestination.Details> { destination ->
                    TaskDetailsScreen(
                        title = destination.title,
                        onBack = { backStack.removeAt(backStack.lastIndex) },
                    )
                }
            },
    )
}
