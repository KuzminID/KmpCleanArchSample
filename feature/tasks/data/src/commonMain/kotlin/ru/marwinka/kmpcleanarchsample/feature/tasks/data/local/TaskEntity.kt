package ru.marwinka.kmpcleanarchsample.feature.tasks.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Кэш задач с сервера. Целиком заменяется при каждом `refresh`. */
@Entity(tableName = "task")
internal data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    /** Порядок, в котором задачи пришли с сервера. */
    val position: Int,
)

/**
 * Отметка пользователя «выполнено». Сервер её не принимает (API только на чтение),
 * поэтому она живёт в отдельной таблице и не затрагивается заменой кэша.
 */
@Entity(tableName = "task_completion")
internal data class TaskCompletionEntity(
    @PrimaryKey val taskId: String,
    val completedAt: Long,
)

/** Строка запроса выполненных задач: задача из кэша и время отметки. */
internal data class CompletedTaskRow(
    val id: String,
    val title: String,
    val completedAt: Long,
)
