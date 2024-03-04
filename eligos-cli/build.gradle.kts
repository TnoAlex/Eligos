import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

dependencies {
    api(project(":eligos-core"))
    runtimeOnly(project(":eligos-processor"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    implementation(project(":kt-references-analysis:analysis-api"))
    implementation(project(":kt-references-analysis:analysis-api-fe10"))
    implementation(project(":kt-references-analysis:analysis-api-impl-base"))
    implementation(project(":kt-references-analysis:analysis-internal-utils"))
    implementation(project(":kt-references-analysis:kt-references-fe10"))
    implementation(project(":kt-references-analysis:project-structure"))
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