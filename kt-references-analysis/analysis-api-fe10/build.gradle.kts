import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":kt-references-analysis:analysis-api-impl-base"))
    api(project(":kt-references-analysis:analysis-internal-utils"))
    api(project(":kt-references-analysis:kt-references-fe10"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
}


tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        freeCompilerArgs += "-Xcontext-receivers"
        freeCompilerArgs += "-opt-in=org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals"
    }
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


