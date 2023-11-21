package com.github.tnoalex.foundation.common

import kotlin.reflect.KClass

interface Container<K : Any, V : Any> : BaseContainer<K, V> {
    override fun getByKey(key: K): V?
    override fun getByType(clazz: KClass<out V>): V?
}