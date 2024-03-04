
dependencies {
    api(project(":eligos-core"))
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("cn.emergentdesign.se:depends-kotlin:1.0.0-SNAPSHOT")
    implementation("cn.emergentdesign.se:depends-core:0.9.8-SNAPSHOT")
    implementation("cn.emergentdesign.se:depends-java:0.9.8-SNAPSHOT")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
