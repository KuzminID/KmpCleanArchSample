package ru.marwinka.kmpcleanarchsample.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import ru.marwinka.kmpcleanarchsample.core.AppError
import ru.marwinka.kmpcleanarchsample.core.AppResult
import ru.marwinka.kmpcleanarchsample.core.testing.RecordingLogger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NetworkResultTest {
    private val logger = RecordingLogger()

    private fun clientResponding(status: HttpStatusCode) =
        createHttpClient(MockEngine { respond(content = "body", status = status) }, logger, baseUrl = "https://example.test/")

    private suspend fun requestWith(status: HttpStatusCode): AppResult<String> {
        val client = clientResponding(status)
        return networkResultOf(logger) { client.get("path").bodyAsText() }
    }

    @Test
    fun successful_response_is_returned_as_success() =
        runTest {
            assertEquals(AppResult.Success("body"), requestWith(HttpStatusCode.OK))
        }

    @Test
    fun http_404_becomes_not_found() =
        runTest {
            assertIs<AppError.NotFound>(assertIs<AppResult.Failure>(requestWith(HttpStatusCode.NotFound)).error)
        }

    @Test
    fun http_5xx_becomes_network_error() =
        runTest {
            assertIs<AppError.Network>(assertIs<AppResult.Failure>(requestWith(HttpStatusCode.ServiceUnavailable)).error)
            assertTrue(logger.errors.isEmpty())
        }

    @Test
    fun io_exception_becomes_network_error() =
        runTest {
            val result = networkResultOf<String>(logger) { throw IOException("no route to host") }

            assertIs<AppError.Network>(assertIs<AppResult.Failure>(result).error)
        }

    @Test
    fun unexpected_exception_becomes_logged_unknown_error() =
        runTest {
            val exception = IllegalStateException("broken json")

            val result = networkResultOf<String>(logger) { throw exception }

            assertIs<AppError.Unknown>(assertIs<AppResult.Failure>(result).error)
            assertEquals(listOf("broken json"), logger.errors.map { it?.message })
        }

    @Test
    fun cancellation_is_rethrown() =
        runTest {
            assertFailsWith<CancellationException> {
                networkResultOf<String>(logger) { throw CancellationException("cancelled") }
            }
        }
}
