package ru.marwinka.kmpcleanarchsample.core

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

val coreModule =
    module {
        singleOf(::DefaultDispatcherProvider) bind DispatcherProvider::class
        singleOf(::PrintLogger) bind Logger::class
        single<Clock> { Clock.System }
    }
