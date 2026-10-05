package ru.marwinka.kmpcleanarchsample.core.database

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import ru.marwinka.kmpcleanarchsample.core.DispatcherProvider

/**
 * Общие настройки любой базы приложения: встроенный SQLite одинаковой версии на всех платформах
 * и запросы на внедрённом IO-диспатчере, а не на main.
 */
fun <T : RoomDatabase> RoomDatabase.Builder<T>.withAppDefaults(dispatchers: DispatcherProvider): RoomDatabase.Builder<T> =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatchers.io)
