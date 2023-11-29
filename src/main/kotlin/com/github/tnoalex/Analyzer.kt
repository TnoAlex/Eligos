package com.github.tnoalex

import com.github.tnoalex.formatter.IFormatter
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.listener.AstListenerContainer
import com.github.tnoalex.listener.FileListener
import com.github.tnoalex.processor.AstProcessorContainer
import depends.extractor.LangProcessorRegistration
import depends.relations.BindingResolver
import depends.relations.IBindingResolver
import depends.relations.RelationCounter
import org.slf4j.LoggerFactory

class Analyzer(
    private val formatter: IFormatter,
    private val languages: List<String?>
) {
    private val contexts = HashMap<String, Context>()
    fun analyze() {
        cleanUpBeforeStart()
        buildAndParserAst()
    }

    fun getContextByLang(languages: String) = contexts[languages]
    private fun cleanUpBeforeStart() {
        logger.info("------ Clean up framework ------")
        AstProcessorContainer.getKeys().filter { !languages.contains(it) }.filter { it != "any" }.forEach {
            AstProcessorContainer.cleanUpProcessor(it)
        }
        logger.info("Finished")
    }

    private fun buildAndParserAst() {
        languages.filterNotNull().forEach { lang ->
            logger.info("Create context for $lang")
            val context = Context(lang)
            AstProcessorContainer.registerByLang(lang, context)
            val langProcessor = LangProcessorRegistration.getRegistry().getProcessorOf(lang)
            langProcessor.addExtraListener(FileListener)
            langProcessor.addExtraListener(AstListenerContainer.getByKey(lang))
            logger.info("------ Build dependencies ------")
            val bindingResolver: IBindingResolver = BindingResolver(langProcessor, false, true)
            val entityRepo =
                langProcessor.buildDependencies(FileContainer.sourceFilePath!!.path, arrayOf(), bindingResolver)
            RelationCounter(entityRepo, langProcessor, bindingResolver).computeRelations()
            context.setDependsRepo(entityRepo)
            contexts[lang] = context
            logger.info("Finished")
            logger.info("------ Clean up framework ------")
            AstProcessorContainer.unregistersByLang(lang)
            AstProcessorContainer.cleanUpProcessor(lang)
            langProcessor.removeExtraListener(FileListener)
            langProcessor.removeExtraListener(AstListenerContainer.getByKey(lang))
            logger.info("Finished")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Analyzer::class.java)
    }
}