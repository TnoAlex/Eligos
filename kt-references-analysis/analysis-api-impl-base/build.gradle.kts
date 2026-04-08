import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api(project(":kt-references-analysis:analysis-api"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
}
kotlin.compilerOptions {
    freeCompilerArgs.addAll(
        "-Xcontext-receivers",
        "-opt-in=org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals"
    )
}

sourceSets {
    main {

    }
}
