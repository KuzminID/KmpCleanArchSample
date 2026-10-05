package ru.marwinka.kmpcleanarchsample.core.database

import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

/** JVM-таргет нужен для тестов на Linux; по умолчанию база кладётся во временный каталог. */
actual class DatabasePathProvider(
    private val directory: File = File(System.getProperty("java.io.tmpdir")),
) {
    actual fun path(fileName: String): String = File(directory, fileName).absolutePath
}

actual val databaseModule: Module =
    module {
        single { DatabasePathProvider() }
    }
