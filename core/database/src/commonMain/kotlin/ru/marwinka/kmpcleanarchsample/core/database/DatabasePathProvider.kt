package ru.marwinka.kmpcleanarchsample.core.database

import org.koin.core.module.Module

/**
    Получение пути для файла БД
    Сам модуль core не занимается инициализацией БД
*/
expect class DatabasePathProvider {
    fun path(fileName: String): String
}

expect val databaseModule: Module
