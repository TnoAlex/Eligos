package com.github.tnoalex.foundation.common

import kotlin.reflect.KClass

interface BaseContainer<T : Any> {
    fun register(entity: T)
    fun getByKey(key: String): Any?
    fun getKeys(): List<String>
    fun getByType(clazz: KClass<out T>): Any?
}