package com.github.tnoalex.foundation.common

import kotlin.reflect.KClass

interface BaseContainer<K : Any, V : Any> {
    fun register(entity: V)
    fun getByKey(key: K): Any?
    fun getKeys(): List<K>
    fun getByType(clazz: KClass<out V>): Any?
}