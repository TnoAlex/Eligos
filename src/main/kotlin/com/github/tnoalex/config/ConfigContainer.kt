package com.github.tnoalex.config

import com.github.tnoalex.foundation.common.Container
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object ConfigContainer : Container<String, AbstractConfig> {

    private val rules = HashMap<String, AbstractConfig>()

    override fun getByType(clazz: KClass<out AbstractConfig>): AbstractConfig {
        return rules.entries.first { it.value.javaClass == clazz.java }.value
    }

    override fun register(entity: AbstractConfig) {
        rules[entity.ruleName] = entity
        logger.info("Registered config: ${entity.ruleName}")
    }

    private val logger = LoggerFactory.getLogger(ConfigContainer::class.java)

    override fun getByKey(key: String): AbstractConfig? = rules[key]

    override fun getKeys(): List<String> = rules.keys.toList()
}