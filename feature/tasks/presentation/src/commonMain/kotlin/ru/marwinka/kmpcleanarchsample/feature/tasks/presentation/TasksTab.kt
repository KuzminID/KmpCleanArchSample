package ru.marwinka.kmpcleanarchsample.feature.tasks.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition

object TasksTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(index = 0u, title = "Задачи")

    @Composable
    override fun Content() {
        Navigator(TasksScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}
