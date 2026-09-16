package ru.marwinka.kmpcleanarchsample.feature.tasks.di

import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.TaskRepositoryImpl
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.repository.TaskRepository
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.GetActiveTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.RefreshTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.ToggleTaskDoneUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.TasksScreenModel

val tasksFeatureModule = module {
    single<TaskRepository> { TaskRepositoryImpl(get(), get()) }
    factory { GetActiveTasksUseCase(get()) }
    factory { RefreshTasksUseCase(get()) }
    factory { ToggleTaskDoneUseCase(get()) }
    factory { TasksScreenModel(get(), get(), get()) }
}
