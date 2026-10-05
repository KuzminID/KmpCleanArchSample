package ru.marwinka.kmpcleanarchsample.core.database

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual class DatabasePathProvider(
    /** Нужен Room-builder'у на Android, поэтому открыт для платформенного кода фич. */
    val context: Context,
) {
    actual fun path(fileName: String): String = context.getDatabasePath(fileName).absolutePath
}

actual val databaseModule: Module =
    module {
        single { DatabasePathProvider(androidContext()) }
    }
