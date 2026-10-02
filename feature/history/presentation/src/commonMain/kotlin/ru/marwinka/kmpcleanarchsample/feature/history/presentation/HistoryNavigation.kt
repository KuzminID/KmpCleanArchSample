package ru.marwinka.kmpcleanarchsample.feature.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

sealed interface HistoryDestination {
    data object List : HistoryDestination
}

/** The history tab: a single destination, hosted in NavDisplay so its ViewModel is scoped like any other screen. */
@Composable
fun HistoryTab(modifier: Modifier = Modifier) {
    val backStack = remember { mutableStateListOf<HistoryDestination>(HistoryDestination.List) }
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
                entry<HistoryDestination.List> { HistoryRoute() }
            },
    )
}
