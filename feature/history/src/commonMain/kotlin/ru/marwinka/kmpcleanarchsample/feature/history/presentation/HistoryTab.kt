package ru.marwinka.kmpcleanarchsample.feature.history.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition

object HistoryTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(index = 1u, title = "История")

    @Composable
    override fun Content() {
        Navigator(HistoryScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}
