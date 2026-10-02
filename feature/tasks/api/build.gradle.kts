plugins {
    id("kmpcleanarchsample.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api, так как Flow входит в публичный контракт
            api(libs.kotlinx.coroutines.core)
        }
    }
}
