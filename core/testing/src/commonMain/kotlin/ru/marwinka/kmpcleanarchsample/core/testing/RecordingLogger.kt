package ru.marwinka.kmpcleanarchsample.core.testing

import ru.marwinka.kmpcleanarchsample.core.Logger

/** Логгер, который запоминает ошибки, чтобы тест мог проверить, что они залогированы. */
class RecordingLogger : Logger {
    private val _errors = mutableListOf<Throwable?>()
    val errors: List<Throwable?> get() = _errors

    override fun debug(
        tag: String,
        message: String,
    ) = Unit

    override fun error(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        _errors += throwable
    }
}
