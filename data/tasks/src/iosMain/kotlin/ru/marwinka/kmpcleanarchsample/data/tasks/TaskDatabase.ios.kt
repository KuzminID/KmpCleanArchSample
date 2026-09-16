package ru.marwinka.kmpcleanarchsample.data.tasks

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.core.DispatcherProvider
import ru.marwinka.kmpcleanarchsample.core.database.DatabasePathProvider

actual val platformTaskDatabaseModule: Module = module {
    single<TaskDatabase> {
        val path = get<DatabasePathProvider>().path("tasks.db")
        Room.databaseBuilder<TaskDatabase>(name = path)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<DispatcherProvider>().io)
            .build()
    }
}
