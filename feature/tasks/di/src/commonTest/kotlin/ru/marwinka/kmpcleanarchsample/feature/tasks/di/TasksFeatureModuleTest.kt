package ru.marwinka.kmpcleanarchsample.feature.tasks.di

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.core.coreModule
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTasksSource
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskDao
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.TasksViewModel
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

private class NoOpTaskDao : TaskDao {
    override fun observeActive(): Flow<List<TaskEntity>> = emptyFlow()

    override fun observeDone(): Flow<List<TaskEntity>> = emptyFlow()

    override suspend fun insertNew(tasks: List<TaskEntity>) = Unit

    override suspend fun markDone(
        id: String,
        completedAt: Long,
    ) = Unit
}

/**
 * Resolves the feature's public entry points with only infrastructure faked, so a constructor change
 * that the module does not satisfy fails here instead of at app start.
 */
class TasksFeatureModuleTest {
    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun module_graph_is_complete() {
        val koin =
            startKoin {
                modules(
                    coreModule,
                    module {
                        single<TaskDao> { NoOpTaskDao() }
                    },
                    tasksFeatureModule,
                )
            }.koin

        assertNotNull(koin.get<TaskRepository>())
        assertNotNull(koin.get<CompletedTasksSource>())
        assertNotNull(koin.get<TasksViewModel>())
    }
}
