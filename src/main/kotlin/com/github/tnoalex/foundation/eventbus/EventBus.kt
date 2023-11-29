package com.github.tnoalex.foundation.eventbus

import com.github.tnoalex.utils.getMethodsAnnotatedWith
import com.github.tnoalex.utils.getMutablePropertiesAnnotateWith
import com.github.tnoalex.utils.invokeMethod
import com.github.tnoalex.utils.invokePropertySetter
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

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

    /**
     * Publishes an event to the listeners.
     *
     * @param event
     * @param targets The event object, which can be of any type.
     * @param targets The list of target listener types.
     * It is used to specify specific recipients when the event object is a Kotlin built-in type, such as String.
     */
    fun post(event: Any, targets: List<KClass<*>>? = null) {
        val methods = listenerMap[event::class] ?: return
        val filteredMethods = methods.filter {
            targets == null || targets.find { t -> t.isSuperclassOf(it.listener::class) } != null
        }
        filteredMethods.forEach { postEvent(it, event) }
    }

    private fun postEvent(wrapper: ListenerMethod, event: Any) {
        if (wrapper.method != null) {
            invokeMethod(wrapper.listener, wrapper.method, arrayOf(event))
        } else {
            wrapper.property?.let { invokePropertySetter(wrapper.listener, it, arrayOf(event)) }
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
        val methodList = getMethodsAnnotatedWith(EventListener::class, listener::class)
            .filter { it.parameters.size == 2 }
            .map {
                ListenerMethod(
                    listener,
                    it,
                    null,
                    it.parameters[1].type.classifier as KClass<*>
                )
            }
        val propertyList = getMutablePropertiesAnnotateWith(EventListener::class, listener::class).map {
            ListenerMethod(
                listener,
                null,
                it,
                it.setter.parameters[1].type.classifier as KClass<*>
            )
        }
        return methodList + propertyList
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