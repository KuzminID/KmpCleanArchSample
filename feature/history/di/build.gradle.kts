plugins {
    id("kmpcleanarchsample.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:history:domain"))
            implementation(project(":feature:history:data"))
            implementation(project(":feature:history:presentation"))
            implementation(libs.koin.core)
        }
    }
}
