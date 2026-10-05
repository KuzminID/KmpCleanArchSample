package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** Маршруты вкладки задач: аргументы — свойства, поэтому навигация типобезопасна. */
@Serializable
sealed interface TasksDestination : NavKey {
    @Serializable
    data object List : TasksDestination

    @Serializable
    data class Details(
        val taskId: String,
        val title: String,
    ) : TasksDestination
}

private val tasksNavigationConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(TasksDestination.List::class)
                    subclass(TasksDestination.Details::class)
                }
            }
    }

/**
 * Back stack вкладки. Вызывается в точке входа, чтобы стек сохранялся при переключении вкладок,
 * а сохраняемое состояние переживало поворот экрана и гибель процесса.
 */
@Composable
fun rememberTasksBackStack(): NavBackStack<NavKey> = rememberNavBackStack(tasksNavigationConfiguration, TasksDestination.List)

/**
 * Вкладка задач. Каждая запись back stack получает свой ViewModelStore, поэтому ViewModel
 * живёт ровно столько, сколько её экран находится в стеке.
 */
@Composable
fun TasksTab(
    backStack: NavBackStack<NavKey>,
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
