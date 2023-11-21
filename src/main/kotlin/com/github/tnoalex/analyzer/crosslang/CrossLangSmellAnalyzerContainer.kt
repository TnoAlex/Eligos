package com.github.tnoalex.analyzer.crosslang

import com.github.tnoalex.foundation.common.Container
import com.github.tnoalex.utils.loadServices
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object CrossLangSmellAnalyzerContainer : Container<Set<String>, AbstractCrossLangAnalyzer> {
    private val analyzers = HashMap<Set<String>, AbstractCrossLangAnalyzer>()
    private val logger = LoggerFactory.getLogger(CrossLangSmellAnalyzerContainer::class.java)

    init {
        val loader = loadServices(AbstractCrossLangAnalyzer::class.java)
        loader.forEach {
            register(it)
            logger.info("Registered analyzer: ${it::class.simpleName}")
        }
    }

    override fun register(entity: AbstractCrossLangAnalyzer) {
        if (analyzers.containsKey(entity.supportLanguage.toSet())) return
        analyzers[entity.supportLanguage.toSet()] = entity
    }

    override fun getByKey(key: Set<String>): AbstractCrossLangAnalyzer? = analyzers[key]

    override fun getByType(clazz: KClass<out AbstractCrossLangAnalyzer>): AbstractCrossLangAnalyzer {
        return analyzers.entries.first { it.value.javaClass == clazz.java }.value
    }

    override fun getKeys(): List<Set<String>> = analyzers.keys.toList()
}