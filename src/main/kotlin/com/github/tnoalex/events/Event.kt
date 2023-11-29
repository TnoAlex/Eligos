package com.github.tnoalex.events

import java.util.*
import kotlin.reflect.KClass

abstract class Event(source: Any?) : EventObject(source) {
    fun checkType(type: KClass<*>): Boolean {
        return source::class == type
    }
}