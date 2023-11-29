package com.github.tnoalex.listener

import com.github.tnoalex.foundation.common.Container
import com.github.tnoalex.utils.loadServices
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object AstListenerContainer : Container<String, AstListener> {
    private val listeners = HashMap<String, AstListener>()
    private val logger = LoggerFactory.getLogger(AstListenerContainer::class.java)

    init {
        val loader = loadServices(AstListener::class.java)
        loader.forEach {
            register(it)
        }
    }

    override fun register(entity: AstListener) {
        listeners[entity.supportLanguage[0]] = entity
        logger.info("Registered listener ${entity::class.simpleName}")
    }

    override fun getByKey(key: String): AstListener? = listeners[key]

    override fun getByType(clazz: KClass<out AstListener>): AstListener {
        return listeners.entries.first { it.value.javaClass == clazz.java }.value
    }

    override fun getKeys(): List<String> = listeners.keys.toList()

}