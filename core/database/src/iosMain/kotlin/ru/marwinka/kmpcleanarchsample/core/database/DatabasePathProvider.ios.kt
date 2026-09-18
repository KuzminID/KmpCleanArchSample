package ru.marwinka.kmpcleanarchsample.core.database

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DatabasePathProvider {
    @OptIn(ExperimentalForeignApi::class)
    actual fun path(fileName: String): String {
        val documentDirectory =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
        return requireNotNull(documentDirectory?.path) { "Document directory is unavailable" } + "/" + fileName
    }
}

actual val databaseModule: Module =
    module {
        single { DatabasePathProvider() }
    }
