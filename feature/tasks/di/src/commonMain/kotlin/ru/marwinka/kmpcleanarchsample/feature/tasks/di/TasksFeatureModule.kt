package ru.marwinka.kmpcleanarchsample.feature.tasks.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTasksSource
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.CompletedTasksSourceImpl
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.TaskRepositoryImpl
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.FakeTaskApi
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.TaskApi
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.CompleteTaskUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.GetActiveTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.RefreshTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.TasksViewModel

val tasksFeatureModule =
    module {
        // TODO: replace the mock with a Ktor-backed TaskApi built on core:network's HttpClient
        single<TaskApi> { FakeTaskApi() }
        singleOf(::TaskRepositoryImpl) bind TaskRepository::class
        singleOf(::CompletedTasksSourceImpl) bind CompletedTasksSource::class
        factoryOf(::GetActiveTasksUseCase)
        factoryOf(::RefreshTasksUseCase)
        factoryOf(::CompleteTaskUseCase)
        viewModelOf(::TasksViewModel)
    }
