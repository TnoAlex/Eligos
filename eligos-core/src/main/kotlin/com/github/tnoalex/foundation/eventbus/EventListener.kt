package com.github.tnoalex.foundation.eventbus

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventListener(
    val filter: String = "",
    val eventPrefix: String = "",
    val filterClazz: Array<KClass<*>> = [],
    val order: Int = 0
)
