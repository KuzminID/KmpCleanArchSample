import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import ru.marwinka.kmpcleanarchsample.buildlogic.libs

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

//Получение имени модуля с заменой сепаратора с '-' на '', то есть вместо design-system получим DesignSystem
private val moduleNamespace = project.path
    .removePrefix(":")
    .split(":")
    .joinToString(".") { it.replace("-", "") }

//Аналогично namespace модуля, но для фреймворка, подключаемого в iosApp
private val frameworkBaseName = project.path
    .removePrefix(":")
    .split(":", "-")
    .joinToString("") { it.replaceFirstChar(Char::uppercase) }

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = frameworkBaseName
            isStatic = true
        }
    }

    android {
        namespace = "ru.marwinka.kmpcleanarchsample.$moduleNamespace"
        compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
    }
}
