package ru.marwinka.kmpcleanarchsample.core.database

import org.koin.core.module.Module

/**
 * Путь к файлу базы на платформе. Сам модуль базы не создаёт: класс базы и её builder
 * объявляет `data`-модуль фичи, которой база принадлежит.
 */
expect class DatabasePathProvider {
    fun path(fileName: String): String
}

expect val databaseModule: Module
