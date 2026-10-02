package ru.marwinka.kmpcleanarchsample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.marwinka.kmpcleanarchsample.designsystem.AppTheme
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.HistoryTab
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.TasksDestination
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.TasksTab
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.tasksStartDestination
import ru.marwinka.kmpcleanarchsample.resources.Res
import ru.marwinka.kmpcleanarchsample.resources.tab_history
import ru.marwinka.kmpcleanarchsample.resources.tab_tasks
import androidx.compose.material3.Tab as MaterialTab

private enum class AppTab(
    val title: StringResource,
) {
    Tasks(Res.string.tab_tasks),
    History(Res.string.tab_history),
}

@Composable
@Preview
fun App() {
    AppTheme {
        var selectedTab by rememberSaveable { mutableIntStateOf(AppTab.Tasks.ordinal) }
        // Hoisted here so the tab keeps its stack (e.g. an open task) while another tab is shown.
        val tasksBackStack = remember { mutableStateListOf<TasksDestination>(tasksStartDestination()) }

        Scaffold(
            modifier = Modifier.safeDrawingPadding(),
            topBar = {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    AppTab.entries.forEach { tab ->
                        MaterialTab(
                            selected = selectedTab == tab.ordinal,
                            onClick = { selectedTab = tab.ordinal },
                            text = { Text(stringResource(tab.title)) },
                        )
                    }
                }
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (AppTab.entries[selectedTab]) {
                    AppTab.Tasks -> TasksTab(backStack = tasksBackStack)
                    AppTab.History -> HistoryTab()
                }
            }
        }
    }
}
