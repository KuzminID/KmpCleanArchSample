package ru.marwinka.kmpcleanarchsample.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

/**
 * `Dispatchers.IO` есть на JVM, Android и Kotlin/Native, но не объявлен в общем коде,
 * поэтому приходит из платформенных source set'ов. Замена через
 * `Dispatchers.Default.limitedParallelism(n)` не работает: при `n` больше числа ядер
 * она возвращает сам `Default`, и блокирующий I/O занимает потоки CPU-задач.
 */
internal expect val ioDispatcher: CoroutineDispatcher

class DefaultDispatcherProvider : DispatcherProvider {
    override val io: CoroutineDispatcher = ioDispatcher
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val main: CoroutineDispatcher = Dispatchers.Main
}
