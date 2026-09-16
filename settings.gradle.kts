rootProject.name = "KMPCleanArchSample"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")
include(":core")
include(":core:network")
include(":core:database")
include(":design-system")
include(":data:tasks")
include(":feature:tasks:domain")
include(":feature:tasks:data")
include(":feature:tasks:presentation")
include(":feature:tasks:di")
include(":feature:history:domain")
include(":feature:history:data")
include(":feature:history:presentation")
include(":feature:history:di")