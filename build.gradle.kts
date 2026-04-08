import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "2.3.20"
}

repositories {
    mavenLocal()
    mavenCentral()
}

allprojects {
    group = "com.github.tnoalex"
    version = "1.0-SNAPSHOT"
}

tasks.jar {
    enabled = false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    kotlin {
        jvmToolchain(17)
    }

    tasks.test {
        useJUnitPlatform()
    }

    configurations.forEach {
        it.exclude(group = "org.slf4j", module = "slf4j-reload4j")
    }

    tasks.withType<Jar> {
        isZip64 = true
    }

    repositories {
        mavenLocal()
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
        maven {
            url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-ide-plugin-dependencies/")
        }
        maven {
            url = uri("https://www.jetbrains.com/intellij-repository/releases/")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public/")
        }
        mavenCentral()
    }
}

