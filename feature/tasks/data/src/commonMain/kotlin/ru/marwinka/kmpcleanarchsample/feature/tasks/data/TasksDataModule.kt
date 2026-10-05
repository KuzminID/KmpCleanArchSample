package ru.marwinka.kmpcleanarchsample.feature.tasks.data

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.core.database.withAppDefaults
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTasksSource
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.ALL_MIGRATIONS
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TasksDatabase
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.tasksDatabaseBuilder
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.KtorTaskApi
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.TaskApi
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository

/**
 * Единственная публичная декларация data-модуля: реализации, Entity, DAO и DTO остаются
 * `internal`, а наружу видны только интерфейсы из `domain` и `api`.
 * Требует от графа `DatabasePathProvider`, `HttpClient`, `DispatcherProvider`, `Logger` и `Clock`.
 */
val tasksDataModule =
    module {
        single<TasksDatabase> {
            tasksDatabaseBuilder(pathProvider = get())
                .withAppDefaults(dispatchers = get())
                .addMigrations(*ALL_MIGRATIONS)
                .build()
        }
        single { get<TasksDatabase>().taskDao() }
        singleOf(::KtorTaskApi) bind TaskApi::class
        singleOf(::TaskRepositoryImpl) bind TaskRepository::class
        singleOf(::CompletedTasksSourceImpl) bind CompletedTasksSource::class
    }
