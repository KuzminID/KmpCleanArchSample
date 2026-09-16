package ru.marwinka.kmpcleanarchsample.data.tasks

import org.koin.dsl.module

val tasksDataModule = module {
    single<TaskApi> { FakeTaskApi() }
    includes(platformTaskDatabaseModule)
    single { get<TaskDatabase>().taskDao() }
}
