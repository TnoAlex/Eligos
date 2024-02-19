package com.github.tnoalex

import com.github.tnoalex.formatter.IFormatter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.filetools.FileHelper
import com.github.tnoalex.listener.AstListener
import com.github.tnoalex.listener.FileListener
import com.github.tnoalex.processor.AstProcessor
import depends.extractor.LangProcessorRegistration
import depends.relations.RelationCounter
import org.slf4j.LoggerFactory

class Analyzer(
    private val formatter: IFormatter,
    private val languages: List<String?>
) {
    private lateinit var context: Context

    fun analyze() {
        buildAndParserAst()
    }

    fun getContext(): Context {
        if (this::context.isInitialized)
            return context
        throw RuntimeException("Context not initialized yet")
    }


    private fun buildAndParserAst() {
        logger.info("Create context ")
        context = Context()
        val astProcessors = ArrayList<AstProcessor>()
        val astListeners = ArrayList<AstListener>()
        ApplicationContext.getBean(AstProcessor::class.java).forEach {
            if (it.supportLanguage.contains("any")) {
                it.registerListener(context)
                astProcessors.add(it)
            } else {
                languages.forEach { l ->
                    if (it.supportLanguage.contains(l)) {
                        it.registerListener(context)
                        astProcessors.add(it)
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

        astProcessors.forEach {
            it.unregisterListener()
        }
        logger.info("Finished")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Analyzer::class.java)
    }
}