group = "com.github.tnoalex"
version = "1.0-SNAPSHOT"

val ktVersion = "2.3.20"

dependencies {
    api("org.jetbrains.kotlin:analysis-api-for-ide:$ktVersion") { isTransitive = false }
    api("org.jetbrains.kotlin:analysis-api-k2-for-ide:$ktVersion") { isTransitive = false }

    implementation("org.jetbrains.kotlin:analysis-api-impl-base-for-ide:$ktVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:analysis-api-platform-interface-for-ide:$ktVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:low-level-api-fir-for-ide:$ktVersion") { isTransitive = false }
    implementation("org.jetbrains.kotlin:symbol-light-classes-for-ide:$ktVersion") { isTransitive = false }
    //testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}