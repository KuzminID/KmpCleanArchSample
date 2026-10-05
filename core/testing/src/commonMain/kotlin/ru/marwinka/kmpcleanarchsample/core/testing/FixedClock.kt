package ru.marwinka.kmpcleanarchsample.core.testing

import kotlin.time.Clock
import kotlin.time.Instant

/** Часы с заданным временем: тесты не зависят от системных часов. */
class FixedClock(
    var now: Instant = Instant.fromEpochMilliseconds(0),
) : Clock {
    override fun now(): Instant = now
}
