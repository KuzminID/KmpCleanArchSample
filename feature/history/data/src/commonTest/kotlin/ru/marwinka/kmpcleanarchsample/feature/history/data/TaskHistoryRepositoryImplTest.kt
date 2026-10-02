package ru.marwinka.kmpcleanarchsample.feature.history.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ru.marwinka.kmpcleanarchsample.feature.history.domain.model.TaskHistoryEntry
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTask
import ru.marwinka.kmpcleanarchsample.feature.tasks.api.CompletedTasksSource
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeCompletedTasksSource(
    private val tasks: List<CompletedTask>,
) : CompletedTasksSource {
    override fun observeCompleted(): Flow<List<CompletedTask>> = flowOf(tasks)
}

class TaskHistoryRepositoryImplTest {
    @Test
    fun maps_completed_tasks_and_computes_duration() =
        runTest {
            val source = FakeCompletedTasksSource(listOf(CompletedTask("1", "Write a test", 1_000, 4_500)))

            val history = TaskHistoryRepositoryImpl(source).observeHistory().first()

            assertEquals(listOf(TaskHistoryEntry("1", "Write a test", 4_500, 3_500)), history)
        }
}
