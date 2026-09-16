package ru.marwinka.kmpcleanarchsample.data.tasks

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "taskEntity")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val status: String,
    val createdAt: Long,
    val completedAt: Long?,
)
