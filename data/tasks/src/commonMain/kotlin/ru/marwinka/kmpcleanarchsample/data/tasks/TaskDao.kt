package ru.marwinka.kmpcleanarchsample.data.tasks

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM taskEntity WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM taskEntity WHERE status = 'DONE' ORDER BY completedAt DESC")
    fun observeDone(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("UPDATE taskEntity SET status = 'DONE', completedAt = :completedAt WHERE id = :id")
    suspend fun markDone(id: String, completedAt: Long)

    @Query("SELECT COUNT(*) FROM taskEntity")
    suspend fun count(): Long
}
