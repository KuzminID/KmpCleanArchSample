package ru.marwinka.kmpcleanarchsample.core.testing

import kotlinx.coroutines.CoroutineDispatcher
import ru.marwinka.kmpcleanarchsample.core.DispatcherProvider

/** Все диспатчеры — один тестовый, обычно `StandardTestDispatcher(testScheduler)` из `runTest`. */
class TestDispatcherProvider(
    dispatcher: CoroutineDispatcher,
) : DispatcherProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
