import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
}

configurations.forEach {
    it.exclude(group = "org.slf4j", module = "slf4j-reload4j")
}


allprojects{
    group = "com.github.tnoalex"
    version = "1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    kotlin{
        jvmToolchain(17)
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    repositories {
        maven {
            url = uri("http://47.115.213.131:8080/repository/alex-snapshots/")
            isAllowInsecureProtocol = true
        }
        maven {
            url = uri("http://47.115.213.131:8080/repository/alex-release/")
            isAllowInsecureProtocol = true
        }
        mavenLocal()
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