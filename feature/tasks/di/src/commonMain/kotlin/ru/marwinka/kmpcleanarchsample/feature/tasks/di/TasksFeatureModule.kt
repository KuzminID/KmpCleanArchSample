package ru.marwinka.kmpcleanarchsample.feature.tasks.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.tasksDataModule
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.CompleteTaskUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.GetActiveTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.usecase.RefreshTasksUseCase
import ru.marwinka.kmpcleanarchsample.feature.tasks.presentation.TasksViewModel

/** Граф фичи: реализации из data, use case из domain и ViewModel из presentation. */
val tasksFeatureModule =
    module {
        includes(tasksDataModule)
        factoryOf(::GetActiveTasksUseCase)
        factoryOf(::RefreshTasksUseCase)
        factoryOf(::CompleteTaskUseCase)
        viewModelOf(::TasksViewModel)
    }
