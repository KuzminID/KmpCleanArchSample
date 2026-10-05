package ru.marwinka.kmpcleanarchsample.feature.history.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.history.domain.repository.TaskHistoryRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTaskHistoryRepository(
    private val entries: List<TaskHistoryEntry>,
) : TaskHistoryRepository {
    override fun observeHistory(): Flow<List<TaskHistoryEntry>> = flowOf(entries)
}

class GetTaskHistoryUseCaseTest {
    @Test
    fun returns_entries_from_repository() =
        runTest {
            val entries = listOf(TaskHistoryEntry(id = "1", title = "S01E01 · Pilot", completedAtEpochMillis = 1_000))
            val useCase = GetTaskHistoryUseCase(FakeTaskHistoryRepository(entries))

            assertEquals(entries, useCase().first())
        }
}
