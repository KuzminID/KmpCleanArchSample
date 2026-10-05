package ru.marwinka.kmpcleanarchsample.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import ru.marwinka.kmpcleanarchsample.core.testing.RecordingLogger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AppResultTest {
    private val logger = RecordingLogger()

    @Test
    fun returns_success_with_the_block_value() =
        runTest {
            assertEquals(AppResult.Success(42), appResultOf(logger) { 42 })
        }

    @Test
    fun unexpected_exception_becomes_logged_unknown_error() =
        runTest {
            val exception = IllegalStateException("boom")

            val result = appResultOf(logger) { throw exception }

            val error = assertIs<AppError.Unknown>(assertIs<AppResult.Failure>(result).error)
            // Сравнение по сообщению: корутины при восстановлении стек-трейса копируют исключение.
            assertEquals("boom", error.cause?.message)
            assertEquals(listOf("boom"), logger.errors.map { it?.message })
        }

    @Test
    fun cancellation_is_rethrown_and_not_turned_into_a_result() =
        runTest {
            assertFailsWith<CancellationException> {
                appResultOf(logger) { throw CancellationException("cancelled") }
            }
            assertTrue(logger.errors.isEmpty())
        }
}
