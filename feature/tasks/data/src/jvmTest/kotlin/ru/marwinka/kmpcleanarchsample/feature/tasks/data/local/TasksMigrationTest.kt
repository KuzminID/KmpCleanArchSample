package ru.marwinka.kmpcleanarchsample.feature.tasks.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Открывает файл базы версии 1 (схема из `schemas/.../1.json`) текущей версией базы.
 * Room сверяет итоговую схему с ожидаемой, поэтому неполная миграция роняет тест.
 */
class TasksMigrationTest {
    private val file = File.createTempFile("tasks-migration", ".db").apply { delete() }

    @AfterTest
    fun tearDown() {
        file.delete()
    }

    @Test
    fun migrates_from_version_1() =
        runTest {
            createVersion1Database()

            val database =
                Room
                    .databaseBuilder<TasksDatabase>(name = file.absolutePath)
                    .setDriver(BundledSQLiteDriver())
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
            val dao = database.taskDao()

            assertEquals(emptyList(), dao.observeActive().first())
            dao.replaceAll(listOf(TaskEntity("1", "S01E01 · Pilot", 0)))
            assertEquals(listOf(TaskEntity("1", "S01E01 · Pilot", 0)), dao.observeActive().first())
            database.close()
        }

    private fun createVersion1Database() {
        val connection = BundledSQLiteDriver().open(file.absolutePath)
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `taskEntity` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                "`status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `completedAt` INTEGER, PRIMARY KEY(`id`))",
        )
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL(
            "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ac790e53dcf6942d03eabc2ad710a65b')",
        )
        connection.execSQL("INSERT INTO taskEntity VALUES('1', 'Набросать граф Gradle-модулей', 'DONE', 0, 10)")
        connection.execSQL("PRAGMA user_version = 1")
        connection.close()
    }
}
