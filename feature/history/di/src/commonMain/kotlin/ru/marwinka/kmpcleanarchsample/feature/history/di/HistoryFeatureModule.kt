package ru.marwinka.kmpcleanarchsample.feature.history.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.feature.history.data.historyDataModule
import ru.marwinka.kmpcleanarchsample.feature.history.domain.usecase.GetTaskHistoryUseCase
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.HistoryViewModel

/** Граф фичи; `CompletedTasksSource` приходит из графа фичи tasks через её `api`. */
val historyFeatureModule =
    module {
        includes(historyDataModule)
        factoryOf(::GetTaskHistoryUseCase)
        viewModelOf(::HistoryViewModel)
    }
