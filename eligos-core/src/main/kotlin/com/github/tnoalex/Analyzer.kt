package com.github.tnoalex

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.parser.FileDistributor
import com.github.tnoalex.processor.PsiProcessor
import com.github.tnoalex.specs.AnalyzerSpec
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class Analyzer(private val analyzerSpec: AnalyzerSpec) {
    val context: Context = ApplicationContext.getExactBean(Context::class.java)!!
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
        val fileDistributor = ApplicationContext.getBean(FileDistributor::class.java)
            .filter { it.launchEnvironment == analyzerSpec.launchEnvironment }.toMutableList()
        val enableAllProcessors = enableAllLangs()
        val it = fileDistributor.iterator()
        while (it.hasNext()) {
            val distributor = it.next()
            if (enableAllProcessors) {
                distributor.init()
            } else {
                listOfNotNull(analyzerSpec.majorLang, analyzerSpec.withLang).forEach { l ->
                    if (distributor.supportLanguage.contains(l)) {
                        distributor.init()
                    } else {
                        ApplicationContext.removeBean(distributor::class.java)
                        it.remove()
                    }
                }
            }
        }
        fileDistributor.forEach {
            it.dispatch()
        }
    }

    private fun registerProcessorEvent() {
        logger.info("Init processors")
        val psiProcessors = HashSet<PsiProcessor>()

        ApplicationContext.getBean(PsiProcessor::class.java).forEach {
            if (it.supportLanguage.contains("any")) {
                it.registerListener()
                psiProcessors.add(it)
            } else {
                listOfNotNull(analyzerSpec.majorLang, analyzerSpec.withLang).forEach { l ->
                    if (it.supportLanguage.contains(l)) {
                        it.registerListener()
                        psiProcessors.add(it)
                    }
                }
            }
        }
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
        }else if (analyzerSpec.debugSpec.enableAnyElse.isNotEmpty()){
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