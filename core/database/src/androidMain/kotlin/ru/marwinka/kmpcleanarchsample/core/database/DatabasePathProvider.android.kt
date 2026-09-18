package ru.marwinka.kmpcleanarchsample.core.database

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual class DatabasePathProvider(
    private val context: android.content.Context,
) {
    actual fun path(fileName: String): String = context.getDatabasePath(fileName).absolutePath
}

actual val databaseModule: Module =
    module {
        single { DatabasePathProvider(androidContext()) }
    }
