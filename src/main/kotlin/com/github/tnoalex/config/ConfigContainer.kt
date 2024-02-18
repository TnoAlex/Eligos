package com.github.tnoalex.config

import com.github.tnoalex.foundation.bean.container.BeanContainer
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object ConfigContainer {

    private val rules = HashMap<String, AbstractConfig>()

    fun getByType(clazz: KClass<out AbstractConfig>): AbstractConfig {
        return rules.entries.first { it.value.javaClass == clazz.java }.value
    }

    fun register(entity: AbstractConfig) {
        rules[entity.ruleName] = entity
        logger.info("Registered config: ${entity.ruleName}")
    }

    private val logger = LoggerFactory.getLogger(ConfigContainer::class.java)

    fun getByKey(key: String): AbstractConfig? = rules[key]

    fun getKeys(): List<String> = rules.keys.toList()
}