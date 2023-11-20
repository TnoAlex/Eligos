package com.github.tnoalex.rules

import com.github.tnoalex.foundation.common.Container
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object RuleContainer : Container<Rule> {

    private val rules = HashMap<String, Rule>()

    override fun getByType(clazz: KClass<out Rule>): Rule {
        return rules.entries.first { it.value.javaClass == clazz.java }.value
    }

    override fun register(entity: Rule) {
        rules[entity.ruleName] = entity
        logger.info("Registered rule: ${entity.ruleName}")
    }

    private val logger = LoggerFactory.getLogger(RuleContainer::class.java)

    override fun getByKey(key: String): Rule? = rules[key]

    override fun getKeys(): List<String> = rules.keys.toList()
}