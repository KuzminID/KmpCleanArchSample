package ru.marwinka.kmpcleanarchsample.feature.history.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.feature.history.data.TaskHistoryRepositoryImpl
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository
import ru.marwinka.kmpcleanarchsample.feature.history.domain.usecase.GetTaskHistoryUseCase
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.HistoryViewModel

val historyFeatureModule =
    module {
        singleOf(::TaskHistoryRepositoryImpl) bind TaskHistoryRepository::class
        factoryOf(::GetTaskHistoryUseCase)
        viewModelOf(::HistoryViewModel)
    }
