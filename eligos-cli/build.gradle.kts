import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

dependencies {
    api(project(":eligos-core"))
    runtimeOnly(project(":eligos-processor"))
    implementation(project(":eligos-formatter"))
    api("org.jetbrains.kotlin:kotlin-compiler:2.3.20")
    implementation(project(":eligos-kotlin-analysis-api"))
    implementation(project(":eligos-kotlin-analysis-api-standalone"))
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation(project(":eligos-issues"))
    testImplementation(project(":eligos-processor"))
}


tasks.withType(ShadowJar::class.java) {
    manifest {
        attributes["Main-Class"] = "com.github.tnoalex.MainKt"
    }
    isZip64 = true
}

tasks.distTar {
    enabled = false
}

tasks.distZip {
    enabled = false
}

tasks.shadowDistTar {
    enabled = false
}

tasks.shadowDistZip {
    enabled = false
}

application {
    mainClass.set("com.github.tnoalex.MainKt")
}