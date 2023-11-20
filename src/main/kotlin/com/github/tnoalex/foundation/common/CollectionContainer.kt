package com.github.tnoalex.foundation.common

import kotlin.reflect.KClass

interface CollectionContainer<T : Any> : BaseContainer<T> {
    override fun getByKey(key: String): Collection<T>?

    override fun getByType(clazz: KClass<out T>): Collection<T>?
}