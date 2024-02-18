package com.github.tnoalex.utils

import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun <T : Any> loadServices(service: Class<T>): ServiceLoader<T> {
    return ServiceLoader.load(service)
}

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