package ru.marwinka.kmpcleanarchsample.feature.tasks.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import ru.marwinka.kmpcleanarchsample.core.AppError
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.core.testing.FixedClock
import ru.marwinka.kmpcleanarchsample.core.testing.RecordingLogger
import ru.marwinka.kmpcleanarchsample.core.testing.TestDispatcherProvider
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskCompletionEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.local.TaskEntity
import ru.marwinka.kmpcleanarchsample.feature.tasks.data.remote.EpisodeDto
import ru.marwinka.kmpcleanarchsample.feature.tasks.domain.model.Task
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class TaskRepositoryImplTest {
    private val cached = listOf(TaskEntity(id = "9", title = "S09E09 · Old", position = 0))
    private val dao = FakeTaskDao(cached)
    private val api = FakeTaskApi()
    private val logger = RecordingLogger()
    private val clock = FixedClock(Instant.fromEpochMilliseconds(1_000))

    private fun TestScope.repository() =
        TaskRepositoryImpl(
            dao = dao,
            api = api,
            clock = clock,
            dispatchers = TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
            logger = logger,
        )

    @Test
    fun observe_active_reads_domain_tasks_from_storage() =
        runTest {
            assertEquals(listOf(Task(id = "9", title = "S09E09 · Old")), repository().observeActive().first())
        }

    @Test
    fun refresh_replaces_the_cache_with_server_tasks_in_server_order() =
        runTest {
            api.result =
                CompletableDeferred(
                    AppResult.Success(listOf(EpisodeDto(1, "Pilot", "S01E01"), EpisodeDto(2, "Lawnmower Dog", "S01E02"))),
                )

            val result = repository().refresh()

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(
                listOf(TaskEntity("1", "S01E01 · Pilot", 0), TaskEntity("2", "S01E02 · Lawnmower Dog", 1)),
                dao.tasks.value,
            )
        }

    @Test
    fun network_error_is_returned_and_cached_tasks_are_kept() =
        runTest {
            api.result = CompletableDeferred(AppResult.Failure(AppError.Network()))

            val result = repository().refresh()

            assertIs<AppError.Network>(assertIs<AppResult.Failure>(result).error)
            assertEquals(cached, dao.tasks.value)
        }

    @Test
    fun cancelled_refresh_is_not_turned_into_a_result_and_keeps_the_cache() =
        runTest {
            api.result = CompletableDeferred()
            var result: AppResult<Unit>? = null
            val job = launch { result = repository().refresh() }
            runCurrent()

            job.cancel()
            runCurrent()

            assertTrue(job.isCancelled)
            assertEquals(null, result)
            assertEquals(cached, dao.tasks.value)
        }

    @Test
    fun storage_failure_during_refresh_becomes_logged_unknown_error() =
        runTest {
            dao.failure = IllegalStateException("disk full")

            val result = repository().refresh()

            assertIs<AppError.Unknown>(assertIs<AppResult.Failure>(result).error)
            // Сравнение по сообщению: при смене диспатчера корутины восстанавливают стек и копируют исключение.
            assertEquals(listOf("disk full"), logger.errors.map { it?.message })
        }

    @Test
    fun complete_records_the_completion_time_from_the_injected_clock() =
        runTest {
            val result = repository().complete("9")

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(listOf(TaskCompletionEntity(taskId = "9", completedAt = 1_000)), dao.completions)
        }
}
