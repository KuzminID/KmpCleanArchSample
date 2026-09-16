package ru.marwinka.kmpcleanarchsample.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

/**
 Количество параллельных потоков диспетчера IO
*/
private const val IO_PARALLELISM = 64

/**
 [Dispatchers.IO] на Kotlin/Native объявлен `internal` — недоступен даже из
 iosMain модуля-потребителя, так что expect/actual тут не помог бы. Кросс-
 платформенная замена — `limitedParallelism` поверх [Dispatchers.Default]:
 тот же общий пул, но с более высоким лимитом одновременных задач, что и
 позволяет не простаивать на блокирующих I/O-вызовах, дожидаясь освобождения
 потоков, занятых CPU-bound работой на [default].
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(IO_PARALLELISM)
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
}
