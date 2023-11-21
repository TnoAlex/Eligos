package com.github.tnoalex.analyzer.singlelang

import com.github.tnoalex.foundation.common.Container
import com.github.tnoalex.utils.loadServices
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass


object SingleSmellAnalyzerContainer : Container<String, AbstractSingleLangAnalyzer> {
    private val logger = LoggerFactory.getLogger(SingleSmellAnalyzerContainer::class.java)
    private val analyzers = HashMap<String, AbstractSingleLangAnalyzer>()

    init {
        val loader = loadServices(AbstractSingleLangAnalyzer::class.java)
        loader.forEach {
            register(it)
            logger.info("Registered analyzer: ${it::class.simpleName}")
        }
    }

    override fun register(entity: AbstractSingleLangAnalyzer) {
        val lang = entity.supportLanguage
        lang.forEach {
            if (analyzers.containsKey(it)) return@forEach
            analyzers[it] = entity
        }
    }

    override fun getByKey(key: String) = analyzers[key.lowercase()]

    override fun getKeys(): List<String> = analyzers.keys.toList()

    override fun getByType(clazz: KClass<out AbstractSingleLangAnalyzer>): AbstractSingleLangAnalyzer {
        return analyzers.entries.first { it.value.javaClass == clazz.java }.value
    }
}