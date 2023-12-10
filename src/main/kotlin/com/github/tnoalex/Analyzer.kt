package com.github.tnoalex

import com.github.tnoalex.formatter.IFormatter
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.listener.AstListenerContainer
import com.github.tnoalex.listener.FileListener
import com.github.tnoalex.processor.AstProcessorContainer
import depends.extractor.LangProcessorRegistration
import depends.relations.RelationCounter
import org.slf4j.LoggerFactory

class Analyzer(
    private val formatter: IFormatter,
    private val languages: List<String?>
) {
    private lateinit var context: Context

    fun analyze() {
        cleanUpBeforeStart()
        buildAndParserAst()
    }

    fun getContext(): Context {
        if (this::context.isInitialized)
            return context
        throw RuntimeException("Context not initialized yet")
    }

    private fun cleanUpBeforeStart() {
        logger.info("------ Clean up framework ------")
        AstProcessorContainer.getKeys().filter { !languages.contains(it) }.filter { it != "any" }.forEach {
            AstProcessorContainer.cleanUpProcessor(it)
        }
        logger.info("Finished")
    }

    private fun buildAndParserAst() {
        logger.info("Create context ")
        context = Context()
        val astListeners = languages.filterNotNull().map {
            AstProcessorContainer.registerByLang(it, context)
            AstListenerContainer.getByKey(it)!!
        }
        val langProcessor = LangProcessorRegistration.getRegistry().getProcessorOf(languages[0])
        langProcessor.addExtraListener(FileListener)
        langProcessor.addExtraListeners(astListeners)
        logger.info("------ Build dependencies ------")
        val bindingResolver = langProcessor.createBindingResolver(false, false)
        val entityRepo =
            langProcessor.buildDependencies(FileContainer.sourceFilePath!!.path, arrayOf(), bindingResolver)
        RelationCounter(entityRepo, langProcessor, bindingResolver).computeRelations()
        logger.info("Finished")
        context.setDependsRepo(entityRepo)
        logger.info("------ Clean up framework ------")
        langProcessor.clearExtraListeners()
        languages.filterNotNull().forEach {
            AstProcessorContainer.unregistersByLang(it)
            AstProcessorContainer.cleanUpProcessor(it)
        }
        logger.info("Finished")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Analyzer::class.java)
    }
}