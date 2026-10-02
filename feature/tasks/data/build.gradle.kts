plugins {
    id("kmpcleanarchsample.kmp.library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            api(project(":feature:tasks:api"))
            implementation(project(":feature:tasks:domain"))
            // api: TaskEntity/TaskDao are Room declarations compiled into AppDatabase in :shared
            api(libs.androidx.room3.runtime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
