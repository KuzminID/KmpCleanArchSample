plugins {
    id("kmpcleanarchsample.kmp.library")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom3)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":feature:tasks:api"))
            implementation(project(":feature:tasks:domain"))
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
        jvmTest.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

// База принадлежит фиче: схема экспортируется рядом с её data-модулем.
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
    add("kspIosArm64", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
}
