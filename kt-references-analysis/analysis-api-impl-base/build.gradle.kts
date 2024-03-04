import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
