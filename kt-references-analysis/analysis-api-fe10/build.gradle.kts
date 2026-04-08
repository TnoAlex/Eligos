
dependencies {
    api(project(":kt-references-analysis:analysis-api-impl-base"))
    api(project(":kt-references-analysis:analysis-internal-utils"))
    api(project(":kt-references-analysis:kt-references-fe10"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
}

kotlin.compilerOptions {
    freeCompilerArgs.addAll(
        "-opt-in=kotlin.RequiresOptIn",
        "-Xcontext-receivers",
        "-opt-in=org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals"
    )
}

sourceSets {
    main {

    }
}


