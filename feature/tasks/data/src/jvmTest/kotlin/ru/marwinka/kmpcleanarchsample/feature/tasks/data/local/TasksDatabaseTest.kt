package ru.marwinka.kmpcleanarchsample.feature.tasks.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Настоящие запросы DAO на Room в памяти. */
class TasksDatabaseTest {
    private val database =
        Room
            .inMemoryDatabaseBuilder<TasksDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    private val dao = database.taskDao()

    @AfterTest
    fun tearDown() = database.close()

    @Test
    fun replace_all_removes_tasks_deleted_on_the_server() =
        runTest {
            dao.replaceAll(listOf(TaskEntity("1", "One", 0), TaskEntity("2", "Two", 1)))

            dao.replaceAll(listOf(TaskEntity("2", "Two", 0)))

            assertEquals(listOf(TaskEntity("2", "Two", 0)), dao.observeActive().first())
        }

    @Test
    fun completed_task_leaves_the_active_list_and_survives_a_cache_replacement() =
        runTest {
            dao.replaceAll(listOf(TaskEntity("1", "One", 0), TaskEntity("2", "Two", 1)))
            dao.insertCompletion(TaskCompletionEntity(taskId = "1", completedAt = 100))

            dao.replaceAll(listOf(TaskEntity("1", "One", 0), TaskEntity("2", "Two", 1)))

            assertEquals(listOf(TaskEntity("2", "Two", 1)), dao.observeActive().first())
            assertEquals(listOf(CompletedTaskRow("1", "One", 100)), dao.observeCompleted().first())
        }

    @Test
    fun completed_tasks_are_ordered_from_newest_and_repeated_completion_keeps_the_first_time() =
        runTest {
            dao.replaceAll(listOf(TaskEntity("1", "One", 0), TaskEntity("2", "Two", 1)))
            dao.insertCompletion(TaskCompletionEntity(taskId = "1", completedAt = 100))
            dao.insertCompletion(TaskCompletionEntity(taskId = "2", completedAt = 200))
            dao.insertCompletion(TaskCompletionEntity(taskId = "1", completedAt = 300))

            assertEquals(
                listOf(CompletedTaskRow("2", "Two", 200), CompletedTaskRow("1", "One", 100)),
                dao.observeCompleted().first(),
            )
        }
}
