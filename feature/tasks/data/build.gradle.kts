plugins {
    id("kmpcleanarchsample.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":feature:tasks:domain"))
            // api, т.к. TaskDao/TaskApi входят в публичный конструктор TaskRepositoryImpl
            api(project(":data:tasks"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
