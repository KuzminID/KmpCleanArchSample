package ru.marwinka.kmpcleanarchsample.feature.tasks.data

import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTask
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.CompletedTaskRow
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.EpisodeDto
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import kotlin.test.Test
import kotlin.test.assertEquals

class TaskMappersTest {
    @Test
    fun episode_becomes_entity_with_code_and_name_in_title() {
        val entity = EpisodeDto(id = 1, name = "Pilot", episode = "S01E01").toEntity(position = 0)

        assertEquals(TaskEntity(id = "1", title = "S01E01 · Pilot", position = 0), entity)
    }

    @Test
    fun entity_becomes_domain_task() {
        assertEquals(Task(id = "1", title = "S01E01 · Pilot"), TaskEntity("1", "S01E01 · Pilot", 0).toDomain())
    }

    @Test
    fun completed_row_becomes_api_model() {
        assertEquals(
            CompletedTask(id = "1", title = "S01E01 · Pilot", completedAtEpochMillis = 42),
            CompletedTaskRow(id = "1", title = "S01E01 · Pilot", completedAt = 42).toCompletedTask(),
        )
    }
}
