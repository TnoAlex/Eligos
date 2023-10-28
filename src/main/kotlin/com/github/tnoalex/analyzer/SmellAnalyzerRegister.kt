package com.github.tnoalex.analyzer

import com.github.tnoalex.utils.getAllSubClass
import org.slf4j.LoggerFactory
import kotlin.reflect.full.primaryConstructor


class SmellAnalyzerRegister private constructor() {
    private val analyzers = HashMap<String, AbstractSmellAnalyzer>()

    fun register(analyzer: AbstractSmellAnalyzer) {
        val lang = analyzer.supportedLanguages.lowercase()
        if (analyzers.containsKey(lang)) return
        analyzers[lang] = analyzer
    }

    fun getAnalyzerByLang(lang: String) = analyzers[lang.lowercase()]

    fun getAllSupportedLanguages() = analyzers.keys.toList()

    init {
        register()
    }

    private fun register() {
        val classes = getAllSubClass(AbstractSmellAnalyzer::class)
        classes.forEach {
            if (it.primaryConstructor != null && it.primaryConstructor!!.parameters.isEmpty()) {
                register((it.primaryConstructor!!.call()) as AbstractSmellAnalyzer)
            } else {
                logger.warn("${it.simpleName} : There is a non-empty primary constructor that cannot be auto-registered")
            }
        }
    }


    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(SmellAnalyzerRegister::class.java)

        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SmellAnalyzerRegister()
        }
    }
}