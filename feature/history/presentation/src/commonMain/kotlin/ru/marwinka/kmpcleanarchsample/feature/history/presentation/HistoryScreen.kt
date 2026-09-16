package ru.marwinka.kmpcleanarchsample.feature.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import ru.marwinka.kmpcleanarchsample.designsystem.TaskCard

/** Экран истории — верхний (и единственный) экран стека [HistoryTab]. */
data object HistoryScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HistoryScreenModel>()
        val state by screenModel.state.collectAsState()

        when (val current = state) {
            is HistoryUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("...")
            }

            is HistoryUiState.Content -> if (current.entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Пока нет завершённых задач")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(current.entries, key = { it.id }) { entry ->
                        TaskCard(
                            title = entry.title,
                            trailingText = "${entry.durationMillis / 60_000} мин",
                        )
                    }
                }
            }
        }
    }
}
