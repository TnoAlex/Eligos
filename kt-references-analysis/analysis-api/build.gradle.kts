
dependencies {
    api(project(":kt-references-analysis:analysis-internal-utils"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
}

sourceSets {
    main {

    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
}