package ru.marwinka.kmpcleanarchsample.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.core.DispatcherProvider
import ru.marwinka.kmpcleanarchsample.core.database.DatabasePathProvider

actual val platformAppDatabaseModule: Module =
    module {
        single<AppDatabase> {
            // File name kept from the former per-feature database so existing installs keep their data.
            val path = get<DatabasePathProvider>().path("tasks.db")
            Room
                .databaseBuilder<AppDatabase>(name = path)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(get<DispatcherProvider>().io)
                .build()
        }
    }
