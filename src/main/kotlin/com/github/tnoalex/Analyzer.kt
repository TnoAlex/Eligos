package com.github.tnoalex

import com.github.tnoalex.formatter.IFormatter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.filetools.FileHelper
import com.github.tnoalex.listener.AstListener
import com.github.tnoalex.listener.FileListener
import com.github.tnoalex.parser.FileDistributor
import com.github.tnoalex.processor.PsiProcessor
import depends.extractor.LangProcessorRegistration
import depends.relations.RelationCounter
import org.slf4j.LoggerFactory

class Analyzer(
    private val formatter: IFormatter,
    private val languages: List<String?>
) {
    val context: Context = Context()

    fun analyze() {
        ApplicationContext.addBean("context", context, SimpleSingletonBeanContainer)
        buildAndParserAst()
        dispatchFiles()
    }

    private fun dispatchFiles() {
        val fileDistributor = ApplicationContext.getBean(FileDistributor::class.java)
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

    private fun buildAndParserAst() {
        logger.info("Create context ")
        val psiProcessors = ArrayList<PsiProcessor>()
        val astListeners = ArrayList<AstListener>()
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

        ApplicationContext.getBean(AstListener::class.java).forEach {
            languages.forEach { l ->
                if (it.supportLanguage.contains(l)) {
                    astListeners.add(it)
                }
            }
        }

        val langProcessor = LangProcessorRegistration.getRegistry().getProcessorOf(languages[0])
        langProcessor.addExtraListener(FileListener)
        langProcessor.addExtraListeners(astListeners.toList())
        logger.info("------ Build dependencies ------")
        val bindingResolver = langProcessor.createBindingResolver(false, false)
        val entityRepo =
            langProcessor.buildDependencies(
                ApplicationContext.getBean(FileHelper::class.java)[0].sourceFilePath.path,
                arrayOf(),
                bindingResolver
            )
        RelationCounter(entityRepo, langProcessor, bindingResolver).computeRelations()
        logger.info("Finished")
        context.setDependsRepo(entityRepo)
        logger.info("------ Clean up framework ------")
        langProcessor.clearExtraListeners()

        psiProcessors.forEach {
            it.unregisterListener()
        }
        logger.info("Finished")
    }

    private fun enableAllProcessor(): Boolean {
        return languages.contains("any")
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(Analyzer::class.java)
    }
}