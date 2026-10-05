package ru.marwinka.kmpcleanarchsample.feature.tasks.data

import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTask
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.CompletedTaskRow
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.EpisodeDto
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task

internal fun EpisodeDto.toEntity(position: Int): TaskEntity =
    TaskEntity(
        id = id.toString(),
        title = "$episode · $name",
        position = position,
    )

internal fun TaskEntity.toDomain(): Task = Task(id = id, title = title)

internal fun CompletedTaskRow.toCompletedTask(): CompletedTask =
    CompletedTask(
        id = id,
        title = title,
        completedAtEpochMillis = completedAt,
    )
