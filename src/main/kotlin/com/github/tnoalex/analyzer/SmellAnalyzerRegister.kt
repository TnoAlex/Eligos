package com.github.tnoalex.analyzer

import com.github.tnoalex.cli.CommandParser
import com.github.tnoalex.utils.getClassAnnotation
import com.github.tnoalex.utils.loadClassByPackageName
import org.slf4j.LoggerFactory
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.system.exitProcess


class SmellAnalyzerRegister private constructor() {
    private val analyzers = HashMap<String, AbstractSmellAnalyzer>()

    fun register(analyzer: AbstractSmellAnalyzer) {
        val lang = analyzer.supportedLanguages.lowercase()
        if (analyzers.containsKey(lang)) return
        analyzers[lang] = analyzer
    }

    fun getAnalyzerByLang(lang: String) = analyzers[lang.lowercase()]

    fun getAllSupportedLanguages() = analyzers.keys.toList()

    fun init() {
        val annotations = getClassAnnotation(CommandParser::class) { it.annotationClass == SmellAnalyzerScanner::class }
        if (annotations.isEmpty()) {
            logger.error("Can not find analyzer base package")
            exitProcess(-1)
        }
        val basePackage = (annotations[0] as SmellAnalyzerScanner).value
        val classes = loadClassByPackageName(basePackage) { it.superclasses.contains(AbstractSmellAnalyzer::class) }

        classes.forEach {
            if (it.superclasses.contains(AbstractSmellAnalyzer::class)) {
                if (it.primaryConstructor != null && it.primaryConstructor!!.parameters.isEmpty()) {
                    register((it.primaryConstructor!!.call()) as AbstractSmellAnalyzer)
                    logger.info("Registered analyzer: ${it.simpleName}")
                } else {
                    logger.warn("${it.simpleName} : There is a non-empty primary constructor that cannot be auto-registered")
                }
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