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
include("eligos-plugin")
include("eligos-cli")
include("eligos-core")
include("eligos-processor")
include(
    ":kt-references-analysis:analysis-api",
    ":kt-references-analysis:analysis-api-fe10",
    ":kt-references-analysis:analysis-api-impl-base",
    ":kt-references-analysis:analysis-internal-utils",
    ":kt-references-analysis:kt-references-fe10",
    ":kt-references-analysis:project-structure")
include("eligos-issues")
