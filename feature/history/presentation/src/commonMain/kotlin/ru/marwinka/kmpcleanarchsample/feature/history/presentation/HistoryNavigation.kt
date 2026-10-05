package ru.marwinka.kmpcleanarchsample.feature.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
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

@Serializable
sealed interface HistoryDestination : NavKey {
    @Serializable
    data object List : HistoryDestination
}

private val historyNavigationConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(HistoryDestination.List::class)
                }
            }
    }

/** Вкладка истории: один экран, но в NavDisplay, чтобы его ViewModel была привязана к записи back stack. */
@Composable
fun HistoryTab(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(historyNavigationConfiguration, HistoryDestination.List)
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
