package ru.marwinka.kmpcleanarchsample.data.tasks

import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String,
    val title: String,
)

interface TaskApi {
    suspend fun fetchTasks(): List<TaskDto>
}

/**
 * Mock-реализация API класса
 */
class FakeTaskApi : TaskApi {
    override suspend fun fetchTasks(): List<TaskDto> {
        delay(400)
        return seedTasks
    }

    private companion object {
        val seedTasks = listOf(
            TaskDto(id = "1", title = "Набросать граф Gradle-модулей"),
            TaskDto(id = "2", title = "Настроить SQLDelight"),
            TaskDto(id = "3", title = "Подключить Koin"),
            TaskDto(id = "4", title = "Собрать iOS-фреймворк"),
        )
    }
}
