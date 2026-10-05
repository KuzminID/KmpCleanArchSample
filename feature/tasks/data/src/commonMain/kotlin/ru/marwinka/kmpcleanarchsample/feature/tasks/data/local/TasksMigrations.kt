package ru.marwinka.kmpcleanarchsample.feature.tasks.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.execSQL

/**
 * Версия 1 хранила демо-задачи из заглушки API вместе со статусом в одной таблице.
 * Версия 2 хранит кэш эпизодов и отдельные отметки о выполнении. Идентификаторы демо-задач
 * не соответствуют эпизодам, поэтому старые строки не переносятся, а таблица удаляется явно.
 */
internal val MIGRATION_1_2 =
    Migration(1, 2) { connection ->
        connection.execSQL("DROP TABLE IF EXISTS `taskEntity`")
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `task` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                "`position` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `task_completion` (`taskId` TEXT NOT NULL, " +
                "`completedAt` INTEGER NOT NULL, PRIMARY KEY(`taskId`))",
        )
    }

internal val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
