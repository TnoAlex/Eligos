
dependencies {
    api(project(":eligos-core"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    implementation(project(":eligos-issues"))
    compileOnly(project(":eligos-kotlin-analysis-api"))
    compileOnly(project(":eligos-kotlin-analysis-api-standalone"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
