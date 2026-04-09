plugins {
    id("java-library")
}
group = "com.github.tnoalex"
version = "1.0-SNAPSHOT"
dependencies {
    // Exclude transitive dependencies due to https://youtrack.jetbrains.com/issue/KT-61639
    api("org.jetbrains.kotlin:analysis-api-standalone-for-ide:2.3.20") { isTransitive = false }
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["apiElements"]) {
    skip()
}
javaComponent.withVariantsFromConfiguration(configurations["runtimeElements"]) {
    skip()
}