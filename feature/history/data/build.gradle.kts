plugins {
    id("kmpcleanarchsample.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":feature:history:domain"))
            // api, так как TaskDao входит в публичный конструктор TaskHistoryRepositoryImpl
            api(project(":data:tasks"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
