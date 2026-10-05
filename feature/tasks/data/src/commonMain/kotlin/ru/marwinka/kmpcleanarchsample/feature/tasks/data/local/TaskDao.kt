package ru.marwinka.kmpcleanarchsample.feature.tasks.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TaskDao {
    @Query(
        """
        SELECT task.* FROM task
        LEFT JOIN task_completion ON task_completion.taskId = task.id
        WHERE task_completion.taskId IS NULL
        ORDER BY task.position
        """,
    )
    fun observeActive(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT task.id AS id, task.title AS title, task_completion.completedAt AS completedAt FROM task
        INNER JOIN task_completion ON task_completion.taskId = task.id
        ORDER BY task_completion.completedAt DESC
        """,
    )
    fun observeCompleted(): Flow<List<CompletedTaskRow>>

    /** Заменяет кэш одной транзакцией: задачи, удалённые на сервере, не остаются в базе. */
    @Transaction
    suspend fun replaceAll(tasks: List<TaskEntity>) {
        deleteAll()
        insertAll(tasks)
    }

    @Query("DELETE FROM task")
    suspend fun deleteAll()

    @Insert
    suspend fun insertAll(tasks: List<TaskEntity>)

    /** Повторная отметка не меняет время первой: операция идемпотентна и выполняется одним запросом. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: TaskCompletionEntity)
}
