package com.github.tnoalex.foundation.eventbus

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty

data class ListenerMethod(
    val listener: Any,
    val method: KFunction<*>?,
    val property: KMutableProperty<*>?,
    val eventType: KClass<*>,
    val filterEl: String,
    val eventPrefix: String,
    val order: Int
)
