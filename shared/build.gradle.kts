plugins {
    id("kmpcleanarchsample.kmp.compose")
}

kotlin {
    android {
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":feature:tasks:presentation"))
            implementation(project(":feature:tasks:di"))
            implementation(project(":feature:history:presentation"))
            implementation(project(":feature:history:di"))
            implementation(project(":design-system"))
            implementation(libs.koin.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

compose.resources {
    packageOfResClass = "ru.marwinka.kmpcleanarchsample.resources"
}
