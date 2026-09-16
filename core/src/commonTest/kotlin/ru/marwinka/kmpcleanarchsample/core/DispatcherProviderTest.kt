package ru.marwinka.kmpcleanarchsample.core

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class DispatcherProviderTest {

    private val dispatchers = DefaultDispatcherProvider()

    @Test
    fun io_and_default_are_distinct_dispatchers() {
        // io — это Dispatchers.Default.limitedParallelism(64), поэтому это
        // самостоятельный (хоть и работающий на общем пуле) диспетчер.
        assertNotSame(dispatchers.default, dispatchers.io)
    }

    @Test
    fun io_allows_more_concurrent_work_than_available_cpu_cores() = runBlocking {
        // На большинстве машин ядер заметно меньше 64 — если бы io был равен
        // Dispatchers.Default, эти задачи выполнялись бы пачками, и общее
        // время сильно превысило бы длительность одной задержки.
        val taskCount = 40
        val elapsed = measureTime {
            (1..taskCount).map {
                async(dispatchers.io) { delay(50.milliseconds) }
            }.awaitAll()
        }
        assertTrue(
            elapsed.inWholeMilliseconds < 200,
            "Ожидался запуск $taskCount задач одним пулом за ~50 мс, а заняло ${elapsed.inWholeMilliseconds} мс",
        )
    }

    @Test
    fun default_dispatcher_executes_suspending_work_and_returns_result() = runBlocking {
        val result = withContext(dispatchers.default) {
            (1..5).sumOf { it * it }
        }
        assertEquals(55, result)
    }

    @Test
    fun io_dispatcher_runs_several_coroutines_concurrently() = runBlocking {
        lateinit var results: List<Int>
        val elapsed = measureTime {
            results = (1..4).map { index ->
                async(dispatchers.io) {
                    delay(100)
                    index
                }
            }.awaitAll()
        }

        assertEquals(listOf(1, 2, 3, 4), results)
        // Последовательное выполнение заняло бы от 400 мс — если задержка
        // заметно меньше, значит корутины действительно шли параллельно.
        assertTrue(
            elapsed.inWholeMilliseconds < 300,
            "Ожидалось параллельное выполнение, но заняло ${elapsed.inWholeMilliseconds} мс",
        )
    }
}
