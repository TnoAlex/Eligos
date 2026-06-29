import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api(project(":eligos-core"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:2.4.0")
    implementation(project(":eligos-issues"))
    implementation(project(":eligos-kotlin-analysis-api"))
    implementation(project(":eligos-kotlin-analysis-api-standalone"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}