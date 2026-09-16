package ru.marwinka.kmpcleanarchsample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import ru.marwinka.kmpcleanarchsample.designsystem.AppTheme
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.HistoryTab
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.TasksTab
import androidx.compose.material3.Tab as MaterialTab

private val tabs = listOf(TasksTab, HistoryTab)

@Composable
@Preview
fun App() {
    AppTheme {
        TabNavigator(TasksTab) {
            val tabNavigator = LocalTabNavigator.current

            Scaffold(
                modifier = Modifier.safeDrawingPadding(),
                topBar = {
                    PrimaryTabRow(selectedTabIndex = tabs.indexOf(tabNavigator.current)) {
                        tabs.forEach { tab ->
                            MaterialTab(
                                selected = tabNavigator.current == tab,
                                onClick = { tabNavigator.current = tab },
                                text = { Text(tab.options.title) },
                            )
                        }
                    }
                },
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    CurrentTab()
                }
            }
        }
    }
}
