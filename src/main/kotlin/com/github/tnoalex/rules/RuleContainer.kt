package com.github.tnoalex.rules

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class RuleContainer private constructor() {

    private val rules = HashMap<String, Rule>()

    fun allRuleNames() = rules.keys

    fun getRuleByName(name: String) = rules[name]
    fun <T : Rule> getRuleByType(clazz: KClass<T>): Rule {
        return rules.entries.first { it.value.javaClass == clazz.java }.value
    }

    fun registerRule(rule: Rule) {
        rules[rule.ruleName] = rule
        logger.info("Registered rule: ${rule.ruleName}")
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(RuleContainer::class.java)

        @JvmStatic
        val INSTANT by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RuleContainer()
        }
    }
}