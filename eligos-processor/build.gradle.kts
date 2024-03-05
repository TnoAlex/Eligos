
dependencies {
    api(project(":eligos-core"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    implementation(project(":eligos-issues"))
    compileOnly(project(":kt-references-analysis:kt-references-fe10"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
