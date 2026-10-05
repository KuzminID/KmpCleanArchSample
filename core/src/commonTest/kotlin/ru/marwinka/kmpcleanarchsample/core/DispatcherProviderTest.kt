package ru.marwinka.kmpcleanarchsample.core

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class DispatcherProviderTest {
    private val dispatchers = DefaultDispatcherProvider()

    @Test
    fun io_is_not_the_default_dispatcher() {
        // Блокирующий I/O не должен занимать потоки, рассчитанные на CPU-задачи.
        assertNotSame(dispatchers.default, dispatchers.io)
    }

    @Test
    fun io_dispatcher_executes_work_and_returns_result() =
        runTest {
            val result = withContext(dispatchers.io) { (1..5).sumOf { it * it } }

            assertEquals(55, result)
        }
}
