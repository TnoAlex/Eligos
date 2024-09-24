package com.github.tnoalex

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.parser.FileDistributor
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.specs.AnalyzerSpec
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class Analyzer(private val analyzerSpec: AnalyzerSpec) {
    val context: Context = ApplicationContext.getExactBean(Context::class.java)!!.also {
        it.allowConfidenceLevel = analyzerSpec.confidenceLevel
    }
    private var analyzerInitialized = false

    fun analyze() {
        logger.info("Start analyzing")
        if (!analyzerInitialized) {
            ApplicationContext.launchEnvironment = analyzerSpec.launchEnvironment
            ApplicationContext.solveComponentEnv()
            registerProcessorEvent()
            setTopExceptionHandle()
        }
        context.resetContext()
        dispatchFiles()
        analyzerInitialized = true
        logger.info("Analyzing done")
    }

    private fun dispatchFiles() {
        logger.info("Start dispatch files")
        val enableAllProcessors = enableAllLangs()
        val distributors = HashSet<FileDistributor>()
        val preToRemove = HashSet<FileDistributor>()
        val languages =
            listOfNotNull(analyzerSpec.majorLang, analyzerSpec.withLang).map { Language.createFromString(it) }
        ApplicationContext.getBeanOfType(FileDistributor::class.java)
            .filter { it.launchEnvironment == analyzerSpec.launchEnvironment }
            .forEach {
                if (enableAllProcessors) {
                    distributors.add(it)
                } else {
                    if (it.supportLanguage.any { l ->
                            l in languages
                        }) {
                        distributors.add(it)
                    } else {
                        preToRemove.add(it)
                    }
                }
            }
        preToRemove.forEach {
            ApplicationContext.removeBeanOfType(it::class.java)
        }
        distributors.forEach {
            it.init()
            it.dispatch()
        }
    }

    private fun registerProcessorEvent() {
        logger.info("Init processors")
        val psiProcessors = HashSet<PsiProcessor>()
        val preToRemove = HashSet<PsiProcessor>()
        val languages =
            listOfNotNull(analyzerSpec.majorLang, analyzerSpec.withLang).map { Language.createFromString(it) }

        ApplicationContext.getBeanOfType(PsiProcessor::class.java).forEach {
            if (it is IssueProcessor && it.severity.level < analyzerSpec.severityLevel.level) {
                preToRemove.add(it)
                return@forEach
            }
            if (it.supportLanguage.contains(Language.AnyLanguage)) {
                psiProcessors.add(it)
            } else {
                if (it.supportLanguage.all { l ->
                        l in languages
                    }) {
                    psiProcessors.add(it)
                } else {
                    preToRemove.add(it)
                }
            }
        }
        psiProcessors.forEach { it.registerListener() }
        preToRemove.forEach { ApplicationContext.removeBeanOfType(it::class.java) }
        disableProcessorsIfDebug(psiProcessors)
    }

    private fun disableProcessorsIfDebug(processors: HashSet<PsiProcessor>) {
        if (!analyzerSpec.debugSpec.enabledDebug) return
        if (analyzerSpec.debugSpec.disableAnyElse.isNotEmpty()) {
            processors.forEach {
                if (it::class.simpleName in analyzerSpec.debugSpec.disableAnyElse) return@forEach
                logger.debug("Disable ${it::class.simpleName}")
                it.unregisterListener()
            }
        } else if (analyzerSpec.debugSpec.enableAnyElse.isNotEmpty()) {
            processors.forEach {
                if (it::class.simpleName !in analyzerSpec.debugSpec.enableAnyElse) return@forEach
                logger.debug("Disable ${it::class.simpleName}")
                it.unregisterListener()
            }
        }
    }

    private fun enableAllLangs(): Boolean {
        return listOfNotNull(analyzerSpec.majorLang, analyzerSpec.withLang).contains("any")
    }

    private fun setTopExceptionHandle() {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            if (analyzerSpec.debugSpec.enabledDebug) {
                e.printStackTrace()
            } else {
                if (analyzerSpec.exceptionHandler == null) {
                    logger.error("Something went wrong..... Turn on debug to see the details or contact us on github")
                    exitProcess(-1)
                } else {
                    analyzerSpec.exceptionHandler.invoke(t, e)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(Analyzer::class.java)
    }
}