package ru.marwinka.kmpcleanarchsample.feature.history.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.marwinka.kmpcleanarchsample.core.testing.TestDispatcherProvider
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
    fun maps_completed_tasks_to_history_entries() =
        runTest {
            val source = FakeCompletedTasksSource(listOf(CompletedTask("1", "S01E01 · Pilot", 4_500)))
            val repository = TaskHistoryRepositoryImpl(source, TestDispatcherProvider(StandardTestDispatcher(testScheduler)))

            assertEquals(listOf(TaskHistoryEntry("1", "S01E01 · Pilot", 4_500)), repository.observeHistory().first())
        }

    @Test
    fun mapper_keeps_id_title_and_completion_time() {
        assertEquals(
            TaskHistoryEntry("2", "S01E02 · Lawnmower Dog", 7),
            CompletedTask("2", "S01E02 · Lawnmower Dog", 7).toHistoryEntry(),
        )
    }
}
