package ru.marwinka.kmpcleanarchsample.feature.tasks.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import ru.marwinka.kmpcleanarchsample.core.coreModule
import ru.marwinka.kmpcleanarchsample.core.database.databaseModule
import ru.marwinka.kmpcleanarchsample.core.network.networkModule
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTasksSource
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.TasksViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Разрешает публичные точки входа фичи вместе с инфраструктурными модулями, поэтому
 * изменение конструктора, которое граф не удовлетворяет, падает здесь, а не при запуске.
 * База и HTTP-клиент только создаются: файл не открывается, запросы не уходят,
 * а обновление в init ViewModel не стартует, пока тестовый Main не продвинут.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TasksFeatureModuleTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun module_graph_is_complete() {
        val koin =
            startKoin {
                modules(coreModule, networkModule, databaseModule, tasksFeatureModule)
            }.koin

        assertNotNull(koin.get<TaskRepository>())
        assertNotNull(koin.get<CompletedTasksSource>())
        assertNotNull(koin.get<TasksViewModel>())
    }
}
