package com.github.tnoalex.rules

import com.github.tnoalex.utils.loadServices
import com.github.tnoalex.utils.setClassProperty
import org.yaml.snakeyaml.Yaml
import java.io.File

object RulerParser {
    fun parserRules(ruleFile: File?) {
        val defaultRuleInputStream = Thread.currentThread().contextClassLoader.getResourceAsStream("rules.yaml")
        val yaml = Yaml()
        val defaultRules: HashMap<String, Any?> = yaml.load(defaultRuleInputStream)
        if (ruleFile != null) {
            val userRules: HashMap<String, Any?> = yaml.load(ruleFile.inputStream())
            margeRules(defaultRules, userRules)
        }
        registerToContainer(defaultRules)
    }

    @Suppress("UNCHECKED_CAST")
    private fun margeRules(defaultRules: HashMap<String, Any?>, userRules: Map<String, Any?>) {
        for (key in userRules.keys) {
            if (defaultRules.containsKey(key) && defaultRules[key] is HashMap<*, *> && userRules[key] is HashMap<*, *>) {
                margeRules(defaultRules[key] as HashMap<String, Any?>, userRules[key] as HashMap<String, Any?>)
            } else {
                defaultRules[key] = userRules[key]
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun registerToContainer(rules: HashMap<String, Any?>) {
        val loader = loadServices(Rule::class.java)
        loader.forEach {
            val rule = rules[it.ruleName.lowercase()] as Map<String, Any?>
            rule.forEach { r ->
                setClassProperty(r.key, r.value, it)
            }
            RuleContainer.INSTANT.registerRule(it)
        }
    }
}