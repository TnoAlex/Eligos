plugins {
    id("java-library")
}
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
    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3") {
        attributes {
            // https://github.com/ben-manes/caffeine/issues/716
            // Remove on upgrade to Caffeine 3.x or if https://youtrack.jetbrains.com/issue/KT-73751 is fixed
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
    // Should be possible to remove when https://youtrack.jetbrains.com/issue/KT-81457 fixed
    runtimeOnly("org.jetbrains.intellij.deps.kotlinx:kotlinx-coroutines-core:1.10.2-intellij-1")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}