package com.github.tnoalex.analyzer.singlelang

import com.github.tnoalex.foundation.common.Container
import com.github.tnoalex.utils.loadServices
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass


object SingleSmellAnalyzerContainer : Container<SingleLangAbstractSmellAnalyzer> {
    private val analyzers = HashMap<String, SingleLangAbstractSmellAnalyzer>()
    private val logger = LoggerFactory.getLogger(SingleSmellAnalyzerContainer::class.java)

    init {
        val loader = loadServices(SingleLangAbstractSmellAnalyzer::class.java)
        loader.forEach {
            register(it)
            logger.info("Registered analyzer: ${it::class.simpleName}")
        }
    }

    override fun register(entity: SingleLangAbstractSmellAnalyzer) {
        val lang = entity.supportLanguage.lowercase()
        if (analyzers.containsKey(lang)) return
        analyzers[lang] = entity
    }

    override fun getByKey(key: String): SingleLangAbstractSmellAnalyzer? = analyzers[key.lowercase()]

    fun getAllSupportedLanguages() = analyzers.keys.toList()


    override fun getKeys(): List<String> = analyzers.keys.toList()

    override fun getByType(clazz: KClass<out SingleLangAbstractSmellAnalyzer>): SingleLangAbstractSmellAnalyzer {
        return analyzers.entries.first { it.value.javaClass == clazz.java }.value
    }
}