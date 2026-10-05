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
            implementation(libs.koin.core.viewmodel)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":feature:tasks:api"))
        }
    }
}
