import org.jetbrains.intellij.tasks.RunIdeTask

plugins {
    id("org.jetbrains.intellij") version "1.17.2"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":eligos-core"))
    implementation(project(":eligos-formatter"))
    runtimeOnly(project(":eligos-processor")){
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler")
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

intellij {
    version.set("2023.3.1")
    type.set("IC")
    plugins.set(
        listOf("org.jetbrains.kotlin")
    )
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
    withType<RunIdeTask> {
        println("RunIdeTask")
        jvmArgs("-Xmx2g")
        autoReloadPlugins.set(true)
    }
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}