plugins {
    id("kmpcleanarchsample.kmp.library")
}

// Тестовые фикстуры фичи: подключаются только в тестовые source set'ы (проверяется сборкой).
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":feature:tasks:domain"))
            api(project(":core:testing"))
        }
    }
}
