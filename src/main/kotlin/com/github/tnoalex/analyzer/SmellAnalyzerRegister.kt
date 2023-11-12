package com.github.tnoalex.analyzer

import com.github.tnoalex.utils.loadServices
import org.slf4j.LoggerFactory


class SmellAnalyzerRegister private constructor() {
    private val analyzers = HashMap<String, AbstractSmellAnalyzer>()

    private fun register(analyzer: AbstractSmellAnalyzer) {
        val lang = analyzer.supportedLanguages.lowercase()
        if (analyzers.containsKey(lang)) return
        analyzers[lang] = analyzer
    }

    fun getAnalyzerByLang(lang: String): AbstractSmellAnalyzer? = analyzers[lang.lowercase()]

    fun getAllSupportedLanguages() = analyzers.keys.toList()

    fun init() {
        val loader = loadServices(AbstractSmellAnalyzer::class.java)
        loader.forEach {
            register(it)
            logger.info("Registered analyzer: ${it::class.simpleName}")
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(SmellAnalyzerRegister::class.java)

        @JvmStatic
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SmellAnalyzerRegister()
        }
    }
}