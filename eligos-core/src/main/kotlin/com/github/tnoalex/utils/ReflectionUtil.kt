package com.github.tnoalex.utils

import com.github.tnoalex.foundation.ApplicationContext
import org.reflections.Reflections
import org.reflections.scanners.Scanner
import org.reflections.util.ConfigurationBuilder
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.URL
import kotlin.reflect.*
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun getMethodsAnnotatedWith(annotationKClass: KClass<out Annotation>, targetClass: KClass<*>): List<KFunction<*>> {
    return targetClass.functions.filter { it.annotations.find { a -> a.annotationClass == annotationKClass } != null }
}

fun getMethodAnnotation(annotationKClass: KClass<out Annotation>, method: KFunction<*>): List<Annotation> {
    return method.annotations.filter { it.annotationClass == annotationKClass }
}

fun getPropertyAnnotation(annotationKClass: KClass<out Annotation>, property: KProperty<*>): List<Annotation> {
    return property.annotations.filter { it.annotationClass == annotationKClass }
}

fun getClassPropertyByName(clazz: KClass<*>, name: String): List<KProperty<*>> {
    return clazz.memberProperties.filter { it.name == name }
}

fun getMutablePropertiesAnnotateWith(
    annotationClass: KClass<out Annotation>,
    targetClass: KClass<*>
): List<KMutableProperty<*>> {
    return targetClass.memberProperties.filter { it is KMutableProperty<*> }
        .map { it as KMutableProperty<*> }
        .filter { it.annotations.find { a -> a.annotationClass == annotationClass } != null }
}

fun invokeMethod(instant: Any, method: KFunction<*>, params: Array<Any>) {
    method.isAccessible = true
    try {
        method.call(instant, *params)
    } catch (e: InvocationTargetException) {
        throw RuntimeException("Invoke target error", e.targetException)
    }

}

fun invokePropertySetter(instant: Any, property: KMutableProperty<*>, params: Array<Any>) {
    property.isAccessible = true
    try {
        property.setter.call(instant, *params)
    } catch (e: InvocationTargetException) {
        throw RuntimeException("Invoke target error", e.targetException)
    }
}

fun invokePropertyGetter(instant: Any, property: KProperty<*>): Any? {
    property.isAccessible = true
    try {
        return property.getter.call(instant)
    } catch (e: InvocationTargetException) {
        throw RuntimeException("Invoke target error", e.targetException)
    }
}

fun <T : Any> creatDataClassAndFillProperty(
    propertyValues: Map<String, Any?>,
    clazz: KClass<T>
): T {
    val constructorMap = HashMap<KParameter, Any?>()
    val kFunction = clazz.constructors.first()
    kFunction.parameters.forEach { p ->
        propertyValues[p.name]?.let {
            constructorMap[p] = it
        }
    }
    return kFunction.callBy(constructorMap)
}

private val classPathJars by lazy {
    val jars = ArrayList<URL>()
    val runtimePaths = System.getProperty("java.class.path").split(File.pathSeparator).toList()
    runtimePaths.forEach {
        if (!it.endsWith(".jar")) return@forEach
        val file = File(it)
        jars.add(file.toURI().toURL())
    }
    jars
}

private val entriesPackages by lazy {
    val classLoader = ApplicationContext.currentClassLoader
    val packages = classLoader.getResource("extension/eligos.ext.pkgs")?.readText() ?: ""
    packages.split(System.lineSeparator())
}

internal fun ConfigurationBuilder.addAllJars(): ConfigurationBuilder {
    classPathJars.forEach {
        addUrls(it)
    }
    return this
}

internal fun scanEntries(scanner: Scanner): Reflections {
    return Reflections(
        ConfigurationBuilder()
            .addAllJars()
            .setClassLoaders(arrayOf(ApplicationContext.currentClassLoader))
            .forPackages(*entriesPackages.toTypedArray())
            .addScanners(scanner)
    )
}