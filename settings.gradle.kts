pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "Eligos"
include(":kt-references-fe10",":analysis-api",":analysis-api-impl-base",":project-structure",":analysis-internal-utils")