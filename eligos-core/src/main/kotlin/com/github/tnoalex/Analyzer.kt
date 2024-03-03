package com.github.tnoalex

import com.github.tnoalex.formatter.IFormatter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.parser.FileDistributor
import com.github.tnoalex.processor.PsiProcessor
import org.slf4j.LoggerFactory

class Analyzer(
    private val formatter: IFormatter,
    private val languages: List<String?>,
    private val launchEnvironment: LaunchEnvironment
) {
    val context: Context = ApplicationContext.getExactBean(Context::class.java)!!
    private var analyzerInitialized = false

    fun analyze() {
        if (!analyzerInitialized) {
            ApplicationContext.launchEnvironment = launchEnvironment
            ApplicationContext.solveComponentEnv()
            registerProcessorEvent()
        }
        dispatchFiles()
        analyzerInitialized = true
    }

    private fun dispatchFiles() {
        val fileDistributor = ApplicationContext.getBean(FileDistributor::class.java)
            .filter { it.launchEnvironment == launchEnvironment }.toMutableList()
        val enableAllProcessors = enableAllProcessor()
        val it = fileDistributor.iterator()
        while (it.hasNext()) {
            val distributor = it.next()
            if (enableAllProcessors) {
                distributor.init()
            } else {
                languages.filterNotNull().forEach { l ->
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
        val psiProcessors = ArrayList<PsiProcessor>()

        ApplicationContext.getBean(PsiProcessor::class.java).forEach {
            if (it.supportLanguage.contains("any")) {
                it.registerListener(context)
                psiProcessors.add(it)
            } else {
                languages.forEach { l ->
                    if (it.supportLanguage.contains(l)) {
                        it.registerListener(context)
                        psiProcessors.add(it)
                    }
                }
            }
        }
        psiProcessors.forEach {
            it.unregisterListener()
        }
    }

    private fun enableAllProcessor(): Boolean {
        return languages.contains("any")
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(Analyzer::class.java)
    }
}