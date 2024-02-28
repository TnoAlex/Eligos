import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
//    id("jps-compatible")
}

dependencies {
//    implementation(project(":analysis:kt-references"))

//    implementation(project(":compiler:psi"))
//    implementation(project(":analysis:light-classes-base"))
//    implementation(project(":compiler:frontend.java"))
//    implementation(intellijCore())

//    compileOnly(libs.guava)
    api("org.jetbrains.kotlin:kotlin-compiler:1.9.22")
    implementation(kotlin("stdlib-jdk8"))
}

sourceSets {
    main {

    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(17)
}