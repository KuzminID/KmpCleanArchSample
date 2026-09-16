package ru.marwinka.kmpcleanarchsample.feature.history.di

import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.feature.history.data.TaskHistoryRepositoryImpl
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository
import ru.marwinka.kmpcleanarchsample.feature.history.domain.usecase.GetTaskHistoryUseCase
import ru.marwinka.kmpcleanarchsample.feature.history.presentation.HistoryScreenModel

val historyFeatureModule = module {
    single<TaskHistoryRepository> { TaskHistoryRepositoryImpl(get()) }
    factory { GetTaskHistoryUseCase(get()) }
    factory { HistoryScreenModel(get()) }
}
