package ru.marwinka.kmpcleanarchsample.feature.history.data

import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTask

internal fun CompletedTask.toHistoryEntry(): TaskHistoryEntry =
    TaskHistoryEntry(
        id = id,
        title = title,
        completedAtEpochMillis = completedAtEpochMillis,
    )
