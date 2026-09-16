plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    // RU: это нужно, чтобы плагины не загружались повторно в classloader'е каждого подпроекта
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeCompiler) apply false
}