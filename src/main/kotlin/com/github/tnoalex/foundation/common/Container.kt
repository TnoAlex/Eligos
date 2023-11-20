package com.github.tnoalex.foundation.common

import kotlin.reflect.KClass

interface Container<T : Any> : BaseContainer<T> {
    override fun getByKey(key: String): T?
    override fun getByType(clazz: KClass<out T>): T?
}