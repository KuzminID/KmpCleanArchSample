plugins {
    id("kmpcleanarchsample.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            api(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
