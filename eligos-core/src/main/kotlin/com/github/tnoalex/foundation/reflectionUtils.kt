package com.github.tnoalex.foundation

import org.reflections.util.ConfigurationBuilder
import java.io.File
import java.net.URLClassLoader

internal fun ConfigurationBuilder.addAllJars() : ConfigurationBuilder {
    val runtimePaths = System.getProperty("java.class.path").split(File.pathSeparator).toList()
    runtimePaths.forEach {
        val file = File(it)
        addUrls(file.toURI().toURL())
    }
    return this
}