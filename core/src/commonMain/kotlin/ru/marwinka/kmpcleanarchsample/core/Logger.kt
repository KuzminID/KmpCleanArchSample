package ru.marwinka.kmpcleanarchsample.core

/**
 * Обёртка логгера: фичи и инфраструктура пишут в лог только через неё и получают её
 * через конструктор, поэтому в тестах её можно подменить.
 */
interface Logger {
    fun debug(
        tag: String,
        message: String,
    )

    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )
}

/** Реализация по умолчанию: стандартный вывод, который попадает в Logcat и в консоль Xcode. */
class PrintLogger : Logger {
    override fun debug(
        tag: String,
        message: String,
    ) {
        println("D/$tag: $message")
    }

    override fun error(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        println("E/$tag: $message" + (throwable?.let { "\n${it.stackTraceToString()}" } ?: ""))
    }
}
