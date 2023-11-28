package com.github.tnoalex.utils

import com.github.tnoalex.rules.Rule
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun <T : Any> loadServices(service: Class<T>): ServiceLoader<T> {
    return ServiceLoader.load(service)
}

fun setClassProperty(propertyName: String, value: Any?, clazz: Rule) {
    val property = clazz::class.memberProperties.find { it.name == propertyName }
    if (property != null && property is KMutableProperty<*>) {
        property.setter.isAccessible = true
        property.setter.call(clazz, value)
    }
}

fun getMethodsAnnotatedWith(annotationKClass: KClass<out Annotation>, targetClass: KClass<*>): List<KFunction<*>> {
    return targetClass.functions.filter { it.annotations.find { a -> a.annotationClass == annotationKClass } != null }
}

fun getMutablePropertiesAnnotateWith(
    annotationClass: KClass<out Annotation>,
    targetClass: KClass<*>
): List<KMutableProperty<*>> {
    return targetClass.memberProperties.filter { it is KMutableProperty<*> }
        .map { it as KMutableProperty<*> }
        .filter { it.annotations.find { a -> a.annotationClass == annotationClass } != null }
}

fun invokeMethod(clazz: Any, method: KFunction<*>, params: Array<Any>) {
    method.isAccessible = true
    try {
        method.call(clazz, *params)
    } catch (e: Exception) {
        throw RuntimeException("Can not invoke ${method.name}")
    }

}

fun invokePropertySetter(clazz: Any, property: KMutableProperty<*>, params: Array<Any>) {
    property.isAccessible = true
    try {
        property.setter.call(clazz, *params)
    } catch (e: Exception) {
        throw RuntimeException("Can not set property ${property.name}")
    }

}