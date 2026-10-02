package ru.marwinka.kmpcleanarchsample.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskDao
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskEntity

/**
 * The app's single Room database. It lives in :shared (the composition root) because it has to
 * list every feature's entities; each feature keeps its entities and DAOs in its own `data` module.
 */
@Database(entities = [TaskEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Конкретные реализации БД на платформах отличаются и могут быть найдены в соответствующих платформенных директориях (android/ios Main)
 */
expect val platformAppDatabaseModule: Module

/** Exposes the database and its DAOs to the feature modules' Koin graphs. */
val appDatabaseModule =
    module {
        includes(platformAppDatabaseModule)
        single<TaskDao> { get<AppDatabase>().taskDao() }
    }
