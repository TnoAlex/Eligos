package com.github.tnoalex.utils

import java.io.File
import java.lang.Thread.currentThread
import kotlin.reflect.KClass

fun <T : Any> getAllSubClass(kClass: KClass<T>): ArrayList<KClass<*>> {
    val subClasses = kClass.sealedSubclasses
    val result = ArrayList<KClass<*>>()
    subClasses.forEach {
        val list = ArrayList<KClass<*>>()
        getSubClass(it, list)
        result.addAll(list)
    }
    return result
}

private fun <T : Any> getSubClass(kClass: KClass<T>, list: ArrayList<KClass<*>>) {
    if (kClass.sealedSubclasses.isEmpty()) {
        return
    }
    kClass.sealedSubclasses.forEach {
        list.add(it)
        getSubClass(it, list)
    }
}

fun <T : Any> getClassAnnotation(kClass: KClass<T>, filter: (Annotation) -> Boolean): List<Annotation> {
    return kClass.annotations.filter { filter(it) }
}

fun loadClassByPackageName(packageName: String, filter: (KClass<*>) -> Boolean): ArrayList<KClass<*>> {
    val classLoader = currentThread().contextClassLoader
    val packagePath = packageName.replace(".", "/")
    val resources = classLoader.getResources(packagePath)
    val result = ArrayList<KClass<*>>()
    while (resources.hasMoreElements()) {
        val url = resources.nextElement()
        val file = File(url.file)
        if (file.isDirectory) {
            val classFiles = file.walkTopDown().filter { it.isFile && it.extension == "class" }
            classFiles.forEach { classFile ->
                val className = "$packageName." + classFile.relativeTo(file).path
                    .removeSuffix(".class").replace('/', '.').replace('\\', '.')
                val loadedClass = classLoader.loadClass(className)
                if (!className.contains("$")) {
                    if (filter(loadedClass.kotlin)) {
                        result.add(loadedClass.kotlin)
                    }
                }
            }
        }
    }
    return result
}