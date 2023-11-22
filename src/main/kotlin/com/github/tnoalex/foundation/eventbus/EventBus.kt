package com.github.tnoalex.foundation.eventbus

import com.github.tnoalex.utils.getMethodsAnnotatedWith
import com.github.tnoalex.utils.invokeMethod
import java.util.*
import kotlin.reflect.KClass

/**
 * A simple message bus implemented in observer mode
 *
 * This is not a thread-safe implementation
 */
object EventBus {
    private val listenerMap = HashMap<KClass<*>, LinkedList<ListenerMethod>>()
    private val eventMap = HashMap<Any, LinkedList<KClass<*>>>()

    fun register(listener: Any) {
        getListenerMethod(listener).forEach {
            subscribe(listener, it)
        }
    }

    fun post(event: Any) {
        val methods = listenerMap[event::class] ?: return
        methods.forEach {
            invokeMethod(it.listener::class, it.method, arrayOf(event))
        }
    }

    fun isRegistered(subscriber: Any?): Boolean {
        return eventMap.containsKey(subscriber)
    }

    fun unregister(listener: Any) {
        val eventClass = eventMap[listener] ?: return
        eventClass.forEach {
            val listenerList = listenerMap[it] ?: return
            for (i in listenerList.indices) {
                if (listenerList[i] == listener) {
                    listenerList.remove()
                }
            }
        }
        eventMap.remove(listener)
    }

    private fun getListenerMethod(listener: Any): List<ListenerMethod> {
        return getMethodsAnnotatedWith(EventListener::class, listener::class)
            .filter { it.parameters.size == 1 }
            .map { ListenerMethod(listener, it, it.parameters[0].type.classifier as KClass<*>) }
    }

    private fun subscribe(listener: Any, listenerMethod: ListenerMethod) {
        val methodList = listenerMap[listenerMethod.eventType]
        if (methodList == null) {
            listenerMap[listenerMethod.eventType] = LinkedList(listOf(listenerMethod))
        } else {
            listenerMap[listenerMethod.eventType]?.add(listenerMethod)
        }
        val events = eventMap[listener]
        if (events == null) {
            eventMap[listener] = LinkedList(listOf(listenerMethod.eventType))
        } else {
            eventMap[listener]?.add(listenerMethod.eventType)
        }
    }
}