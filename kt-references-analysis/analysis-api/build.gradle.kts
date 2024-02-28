import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

dependencies {
    api(project(":kt-references-analysis:project-structure"))
    api(project(":kt-references-analysis:analysis-internal-utils"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
}