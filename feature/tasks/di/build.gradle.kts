plugins {
    id("kmpcleanarchsample.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:tasks:domain"))
            implementation(project(":feature:tasks:data"))
            implementation(project(":feature:tasks:presentation"))
            implementation(libs.koin.core)
        }
    }
}
