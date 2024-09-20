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
    val filterClazz: Array<KClass<*>>,
    val eventPrefix: String,
    val order: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListenerMethod

        if (listener != other.listener) return false
        if (method != other.method) return false
        if (property != other.property) return false
        if (eventType != other.eventType) return false
        if (filterEl != other.filterEl) return false
        if (!filterClazz.contentEquals(other.filterClazz)) return false
        if (eventPrefix != other.eventPrefix) return false
        if (order != other.order) return false

        return true
    }

    override fun hashCode(): Int {
        var result = listener.hashCode()
        result = 31 * result + (method?.hashCode() ?: 0)
        result = 31 * result + (property?.hashCode() ?: 0)
        result = 31 * result + eventType.hashCode()
        result = 31 * result + filterEl.hashCode()
        result = 31 * result + filterClazz.contentHashCode()
        result = 31 * result + eventPrefix.hashCode()
        result = 31 * result + order
        return result
    }
}
