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
