package ru.marwinka.kmpcleanarchsample.feature.tasks.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM taskEntity WHERE status = '${TaskStatus.ACTIVE}' ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM taskEntity WHERE status = '${TaskStatus.DONE}' ORDER BY completedAt DESC")
    fun observeDone(): Flow<List<TaskEntity>>

    /** Добавляет только новые задачи: уже существующие (в том числе выполненные) не перезаписываются. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNew(tasks: List<TaskEntity>)

    @Query("UPDATE taskEntity SET status = '${TaskStatus.DONE}', completedAt = :completedAt WHERE id = :id")
    suspend fun markDone(
        id: String,
        completedAt: Long,
    )
}
