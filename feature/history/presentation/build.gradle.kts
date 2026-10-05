plugins {
    id("kmpcleanarchsample.kmp.compose")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":feature:history:domain"))
            implementation(project(":design-system"))
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // api: ViewModel — супертип ViewModel модуля, на него ссылается модуль :di
            api(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}

compose.resources {
    packageOfResClass = "ru.marwinka.kmpcleanarchsample.feature.history.presentation.resources"
}
