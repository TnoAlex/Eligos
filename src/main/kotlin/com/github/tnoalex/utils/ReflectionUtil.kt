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

fun invokeMethod(clazz: KClass<*>, method: KFunction<*>, params: Array<Any>) {
    method.isAccessible = true
    method.call(clazz, *params)
}