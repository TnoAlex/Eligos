package com.github.tnoalex.foundation.eventbus

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventListener(val order: Int = Int.MAX_VALUE)
