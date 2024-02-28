import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.gitlab.arturbosch.detekt").version("1.23.3")
    id("maven-publish")
    application
}

group = "com.github.tnoalex"
version = "1.0-SNAPSHOT"

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

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22")
    implementation("cn.emergentdesign.se:depends-kotlin:1.0.0-SNAPSHOT")
    implementation("cn.emergentdesign.se:depends-core:0.9.8-SNAPSHOT")
    implementation("cn.emergentdesign.se:depends-java:0.9.8-SNAPSHOT")
    implementation("cn.emergentdesign.se:utils:0.1.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.github.ajalt.clikt:clikt:3.5.4")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("org.mvel:mvel2:2.5.0.Final")
    implementation("org.reflections:reflections:0.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}


publishing {
    repositories {
        maven {
            url = uri("http://47.115.213.131:8080/repository/alex-snapshots/")
            credentials {
                username = properties["mavenUsername"].toString()
                password = properties["mavenPassword"].toString()
            }
            isAllowInsecureProtocol = true
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.tnoalex"
            artifactId = "eligos"
            version = "1.0.0-SNAPSHOT"

            from(components["java"])
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("com.github.tnoalex.MainKt")
}

tasks.withType(ShadowJar::class.java) {
    manifest {
        attributes["Main-Class"] = "com.github.tnoalex.MainKt"
    }
}
