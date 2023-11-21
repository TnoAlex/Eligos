package com.github.tnoalex.foundation.common

import kotlin.reflect.KClass

interface CollectionContainer<K : Any, V : Any> : BaseContainer<K, V> {
    override fun getByKey(key: K): Collection<V>?

    override fun getByType(clazz: KClass<out V>): Collection<V>?
}