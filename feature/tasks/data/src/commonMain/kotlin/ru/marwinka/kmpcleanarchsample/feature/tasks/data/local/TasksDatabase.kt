package ru.marwinka.kmpcleanarchsample.feature.tasks.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import ru.marwinka.kmpcleanarchsample.core.database.DatabasePathProvider

/**
 * База фичи задач. Объявлена в её data-модуле, поэтому Entity и DAO остаются `internal`:
 * другие фичи видят выполненные задачи только через `feature:tasks:api`.
 */
@Database(entities = [TaskEntity::class, TaskCompletionEntity::class], version = 2)
@ConstructedBy(TasksDatabaseConstructor::class)
internal abstract class TasksDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        /** Имя файла сохранено с версии 1, когда база была общей для приложения. */
        const val FILE_NAME = "tasks.db"
    }
}

@Suppress("KotlinNoActualForExpect")
internal expect object TasksDatabaseConstructor : RoomDatabaseConstructor<TasksDatabase> {
    override fun initialize(): TasksDatabase
}

/** Платформенная часть builder'а: на Android Room нужен Context. */
internal expect fun tasksDatabaseBuilder(pathProvider: DatabasePathProvider): RoomDatabase.Builder<TasksDatabase>
