package com.github.tnoalex.foundation

import org.reflections.util.ConfigurationBuilder
import java.io.File

internal fun ConfigurationBuilder.addAllJars() : ConfigurationBuilder {
    val resources = Thread.currentThread().contextClassLoader.getResources("")

    while (resources.hasMoreElements()) {
        val resource = resources.nextElement()
        if (resource.protocol == "file") {
            val file = File(resource.file)
            if (file.name.endsWith(".jar")) {
                addUrls(resource.toURI().toURL())
            }
        } else if (resource.protocol == "jar") {
            addUrls(resource.toURI().toURL())
        }
    }
    return this
}