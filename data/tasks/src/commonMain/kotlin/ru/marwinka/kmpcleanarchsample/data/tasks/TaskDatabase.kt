package ru.marwinka.kmpcleanarchsample.data.tasks

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import org.koin.core.module.Module

@Database(entities = [TaskEntity::class], version = 1)
@ConstructedBy(TaskDatabaseConstructor::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object TaskDatabaseConstructor : RoomDatabaseConstructor<TaskDatabase> {
    override fun initialize(): TaskDatabase
}

/**
 * Конкретные реализации БД на платформах отличаются и могут быть найдены в соответствующих платформенных директориях (android/ios Main)
 */
expect val platformTaskDatabaseModule: Module
