package ru.marwinka.kmpcleanarchsample.core

import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val coreModule =
    module {
        single<DispatcherProvider> { DefaultDispatcherProvider() }
        single<Clock> { Clock.System }
    }
