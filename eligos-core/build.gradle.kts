import org.jetbrains.kotlin.incremental.createDirectory
import java.time.ZonedDateTime

dependencies {
    api("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("org.yaml:snakeyaml:2.2")
    api("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.mvel:mvel2:2.5.0.Final")
    implementation("org.reflections:reflections:0.10.2")
}

tasks.register("writeProperties") {
    doLast {
        val properties = mapOf(
            "EligosVersion" to project.version,
            "EligosBuildTime" to ZonedDateTime.now()
        )

        val content = properties.entries.joinToString("\n") { (key, value) ->
            "$key=$value"
        }

        val resourceDir =  File(sourceSets.main.get().output.resourcesDir!!.path.removeSuffix("main"))
        if (!resourceDir.exists()){
            resourceDir.createDirectory()
        }
        val propertyFile = File(resourceDir, "eligos-meta.properties")
        if(!propertyFile.exists()){
            propertyFile.createNewFile()
        }

        propertyFile.writeText(content)
    }
}

tasks.getByName("compileJava") {
    dependsOn("writeProperties")
}
