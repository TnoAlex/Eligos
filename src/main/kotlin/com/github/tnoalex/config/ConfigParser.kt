package com.github.tnoalex.config

import com.github.tnoalex.foundation.bean.Component
import org.yaml.snakeyaml.Yaml
import java.io.File

@Component(order = Int.MAX_VALUE)
class ConfigParser {
    val rules by lazy { parserRules() }
    var extendRules: File? = null

    private fun parserRules(): HashMap<String, Any?> {
        val defaultRuleInputStream = Thread.currentThread().contextClassLoader.getResourceAsStream("config.yaml")
        val yaml = Yaml()
        val defaultRules: HashMap<String, Any?> = yaml.load(defaultRuleInputStream)
        if (extendRules != null) {
            val userRules: HashMap<String, Any?> = yaml.load(extendRules!!.inputStream())
            margeRules(defaultRules, userRules)
        }
        return defaultRules
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
}