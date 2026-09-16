package ru.marwinka.kmpcleanarchsample

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import ru.marwinka.kmpcleanarchsample.core.coreModule
import ru.marwinka.kmpcleanarchsample.core.database.databaseModule
import ru.marwinka.kmpcleanarchsample.core.network.networkModule
import ru.marwinka.kmpcleanarchsample.data.tasks.tasksDataModule
import ru.marwinka.kmpcleanarchsample.feature.history.di.historyFeatureModule
import ru.marwinka.kmpcleanarchsample.feature.tasks.di.tasksFeatureModule

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(
            coreModule,
            networkModule,
            databaseModule,
            tasksDataModule,
            tasksFeatureModule,
            historyFeatureModule,
        )
    }
}

/**
 *
 * Точка входа без параметров, вызываемая из Swift
 */
fun doInitKoin() = initKoin()
