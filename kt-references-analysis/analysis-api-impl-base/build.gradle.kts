import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

dependencies {
    api(project(":kt-references-analysis:analysis-api"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
    kotlinOptions.freeCompilerArgs += "-opt-in=org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals"
}

sourceSets {
    main {

    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(17)
}
