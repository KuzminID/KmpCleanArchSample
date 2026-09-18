package ru.marwinka.kmpcleanarchsample.core

import org.koin.dsl.module

val coreModule =
    module {
        single<DispatcherProvider> { DefaultDispatcherProvider() }
    }
