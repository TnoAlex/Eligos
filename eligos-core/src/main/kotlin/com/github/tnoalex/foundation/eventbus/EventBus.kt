package com.github.tnoalex.foundation.eventbus

import com.github.tnoalex.utils.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * A simple message bus implemented in observer mode
 *
 * This is not a thread-safe implementation
 */
object EventBus {
    private val listenerMap = HashMap<KClass<*>, ArrayList<ListenerMethod>>()
    private val eventMap = HashMap<Any, ArrayList<KClass<*>>>()


    fun register(listener: Any) {
        getListenerMethod(listener).forEach {
            subscribe(listener, it)
        }
        sortEvent()
    }

    /**
     * Publishes an event to the listeners.
     *
     * @param event The event object, which can be of any type.
     * @param targets The list of target listener types.
     * It is used to specify specific recipients when the event object is a Kotlin built-in type, such as String.
     */
    fun post(event: Any, targets: List<KClass<*>>? = null, prefix: String = "") {
        val methods = getEventReceiver(event)
        if (methods.isEmpty()) return
        val filteredMethods = methods.filter {
            targets == null || targets.find { t -> t.isSuperclassOf(it.listener::class) } != null
        }
        filteredMethods.forEach { postEvent(it, event, prefix) }
    }

    private fun getEventReceiver(event: Any): ArrayList<ListenerMethod> {
        val methods = ArrayList<ListenerMethod>()
        listenerMap.keys.forEach {
            if (it.java.isAssignableFrom(event.javaClass)) {
                listenerMap[it]?.let { it1 -> methods.addAll(it1) }
            }
        }
        return methods
    }

    private fun postEvent(wrapper: ListenerMethod, event: Any, prefix: String) {
        if (canPost(wrapper, event, prefix)) {
            invokeTarget(wrapper, event)
        }
    }

    private fun canPost(wrapper: ListenerMethod, event: Any, prefix: String): Boolean {
        var elFlag = false
        var clazzFlag = wrapper.filterClazz.isEmpty()
        var prefixFlag = false
        if (wrapper.filterEl.isBlank() ||
            evaluateBooleanElExpression(
                wrapper.filterEl,
                wrapper.listener,
                event
            )
        ) {
            elFlag = true
        }
        if (wrapper.filterClazz.isNotEmpty()) {
            for (i in wrapper.filterClazz) {
                if (i == event::class || i.isSuperclassOf(event::class)) {
                    clazzFlag = true
                    break
                }
            }
        }
        if (prefix.isBlank() || wrapper.eventPrefix.isBlank() || wrapper.eventPrefix == prefix) {
            prefixFlag = true
        }
        return elFlag && clazzFlag && prefixFlag
    }

    private fun invokeTarget(wrapper: ListenerMethod, event: Any) {
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
            listenerList.removeIf { l -> l.listener == listener }
            listenerMap[it] = listenerList
        }
        eventMap.remove(listener)
    }

    private fun sortEvent() {
        for (key in listenerMap.keys) {
            val distinct = ArrayList(listenerMap[key]!!.distinct())
            distinct.sortByDescending { it.order }
            listenerMap[key] = distinct
        }
    }

    private fun getListenerMethod(listener: Any): List<ListenerMethod> {
        val methodList = getMethodsAnnotatedWith(EventListener::class, listener::class)
            .filter { it.parameters.size == 2 }
            .map {
                val annotation = getMethodAnnotation<EventListener>(it).first()
                ListenerMethod(
                    listener,
                    it,
                    null,
                    it.parameters[1].type.classifier as KClass<*>,
                    annotation.filter,
                    annotation.filterClazz,
                    annotation.eventPrefix,
                    annotation.order
                )
            }
        val propertyList = getMutablePropertiesAnnotateWith(EventListener::class, listener::class)
            .map {
                val annotation = getPropertyAnnotation<EventListener>(it)
                ListenerMethod(
                    listener,
                    null,
                    it,
                    it.setter.parameters[1].type.classifier as KClass<*>,
                    annotation.filter,
                    annotation.filterClazz,
                    annotation.eventPrefix,
                    annotation.order
                )
            }
        return methodList + propertyList
    }

    private fun subscribe(listener: Any, listenerMethod: ListenerMethod) {
        val methodList = listenerMap[listenerMethod.eventType]
        if (methodList == null) {
            listenerMap[listenerMethod.eventType] = ArrayList(listOf(listenerMethod))
        } else {
            listenerMap[listenerMethod.eventType]?.add(listenerMethod)
        }
        val events = eventMap[listener]
        if (events == null) {
            eventMap[listener] = ArrayList(listOf(listenerMethod.eventType))
        } else {
            eventMap[listener]?.add(listenerMethod.eventType)
        }
    }
}