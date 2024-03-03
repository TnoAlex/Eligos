import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

dependencies {
    api(project(":eligos-core"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    runtimeOnly(project(":eligos-processor"))
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.withType(ShadowJar::class.java) {
    manifest {
        attributes["Main-Class"] = "com.github.tnoalex.MainKt"
    }
}

application {
    mainClass.set("com.github.tnoalex.MainKt")
}