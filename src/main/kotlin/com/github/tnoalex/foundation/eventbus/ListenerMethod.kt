package com.github.tnoalex.foundation.eventbus

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class ListenerMethod(
    val listener: Any,
    val method: KFunction<*>,
    val eventType: KClass<*>
)
