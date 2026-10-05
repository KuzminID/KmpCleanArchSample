package ru.marwinka.kmpcleanarchsample.feature.history.di

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.core.coreModule
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.HistoryViewModel
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTask
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTasksSource
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

private class EmptyCompletedTasksSource : CompletedTasksSource {
    override fun observeCompleted(): Flow<List<CompletedTask>> = emptyFlow()
}

/** Граф фичи с fake контракта другой фичи: проверяет, что модуль разрешает свои точки входа. */
class HistoryFeatureModuleTest {
    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun module_graph_is_complete() {
        val koin =
            startKoin {
                modules(
                    coreModule,
                    module { single<CompletedTasksSource> { EmptyCompletedTasksSource() } },
                    historyFeatureModule,
                )
            }.koin

        assertNotNull(koin.get<TaskHistoryRepository>())
        assertNotNull(koin.get<HistoryViewModel>())
    }
}
