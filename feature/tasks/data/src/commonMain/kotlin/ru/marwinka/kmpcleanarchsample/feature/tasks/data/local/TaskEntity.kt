package ru.marwinka.kmpcleanarchsample.feature.tasks.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Значения колонки `status`; единственное место, где они записаны. */
object TaskStatus {
    const val ACTIVE = "ACTIVE"
    const val DONE = "DONE"
}

@Entity(tableName = "taskEntity")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val status: String,
    val createdAt: Long,
    val completedAt: Long?,
)
