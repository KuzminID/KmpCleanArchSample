package ru.marwinka.kmpcleanarchsample.feature.history.data

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository

/** Публичная декларация data-модуля; требует от графа `CompletedTasksSource` и `DispatcherProvider`. */
val historyDataModule =
    module {
        singleOf(::TaskHistoryRepositoryImpl) bind TaskHistoryRepository::class
    }
