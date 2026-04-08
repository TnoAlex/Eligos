
dependencies {
    api(project(":kt-references-analysis:analysis-internal-utils"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
}

sourceSets {
    main {

    }
}

kotlin.compilerOptions {
    freeCompilerArgs.add("-Xcontext-receivers")
}