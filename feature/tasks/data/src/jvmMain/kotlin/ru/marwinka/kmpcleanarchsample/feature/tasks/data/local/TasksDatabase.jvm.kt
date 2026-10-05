package ru.marwinka.kmpcleanarchsample.feature.tasks.data.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import ru.marwinka.kmpcleanarchsample.core.database.DatabasePathProvider

internal actual fun tasksDatabaseBuilder(pathProvider: DatabasePathProvider): RoomDatabase.Builder<TasksDatabase> =
    Room.databaseBuilder<TasksDatabase>(name = pathProvider.path(TasksDatabase.FILE_NAME))
