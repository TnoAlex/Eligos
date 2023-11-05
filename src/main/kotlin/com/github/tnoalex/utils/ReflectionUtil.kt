package com.github.tnoalex.utils

import com.github.tnoalex.rules.Rule
import java.util.*
import kotlin.reflect.KMutableProperty
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