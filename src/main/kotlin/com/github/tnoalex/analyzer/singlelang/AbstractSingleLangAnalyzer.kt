package com.github.tnoalex.analyzer.singlelang

import com.github.tnoalex.analyzer.AnalysisHierarchyEnum
import com.github.tnoalex.analyzer.SmellAnalyzer
import com.github.tnoalex.formatter.FormatterFactory
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.foundation.asttools.listener.AstListenerContainer
import com.github.tnoalex.foundation.asttools.processor.AstProcessorContainer
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.rules.FunctionRule
import com.github.tnoalex.rules.RuleContainer
import com.github.tnoalex.utils.getEntitiesByType
import com.github.tnoalex.utils.toAdjacencyList
import depends.deptypes.DependencyType
import depends.entity.FunctionEntity
import depends.extractor.LangProcessorRegistration
import depends.relations.BindingResolver
import depends.relations.IBindingResolver
import depends.relations.RelationCounter


abstract class AbstractSingleLangAnalyzer : SmellAnalyzer {
    protected var context: SingleLangAnalyzerContext? = null
    override fun analyze() {
        findUselessImport()
        findCircularReferences()
        findTooManyParameters()
    }

    override fun createAnalyticsContext(formatter: FormatterTypeEnum) {
        AstProcessorContainer.hookAstByLang(supportLanguage[0])
        val langProcessor = LangProcessorRegistration.getRegistry().getProcessorOf(supportLanguage[0])
        langProcessor.addExtraListener(AstListenerContainer.getByKey(supportLanguage[0]))
        val bindingResolver: IBindingResolver =
            BindingResolver(langProcessor, false, true)
        val entityRepo =
            langProcessor.buildDependencies(FileContainer.sourceFilePath!!.path, arrayOf(), bindingResolver)
        RelationCounter(entityRepo, langProcessor, bindingResolver).computeRelations()
        context = SingleLangAnalyzerContext(
            supportLanguage[0],
            FormatterFactory.getFormatter(formatter) ?: throw RuntimeException("Unknown result formatter"),
            entityRepo
        )
        AstProcessorContainer.removeHooksByLang(supportLanguage[0])
        langProcessor.removeExtraListener(AstListenerContainer.getByKey(supportLanguage[0]))
    }


    fun findUselessImport() {
        val fileDependency = context?.getDependencyMatrix(AnalysisHierarchyEnum.FILE)
        fileDependency?.run {
            dependencyPairs.filter {
                it.dependencies.size == 1 && it.dependencies.first().type.equals(DependencyType.IMPORT)
            }.forEach {
                context!!.foundUnusedImportPattern(listOf(it.from, it.to))
            }
        }
    }

    fun findCircularReferences() {
        val fileDependency = context?.getDependencyMatrix(AnalysisHierarchyEnum.FILE) ?: return
        val adjacencyList = fileDependency.toAdjacencyList()
        val scc = adjacencyList.solveSCC()
        scc.filter { it.size > 1 }.forEach {
            context!!.foundCircularReferences(it, adjacencyList.subPartOfNodes(it))
        }
    }

    fun findTooManyParameters() {
        val functionEntities = context?.entityRepo?.getEntitiesByType(FunctionEntity::class.java) ?: return
        val functionDependency = context?.getDependencyMatrix(AnalysisHierarchyEnum.METHOD)
        functionEntities.map { it as FunctionEntity }.filter {
            it.parameters.size > (RuleContainer.getByType(FunctionRule::class) as FunctionRule).arity
        }.forEach {
            val file = functionDependency!!.nodes.first { f ->
                f.split("(")[1] == it.qualifiedName + ")"
            }.split("(")[0]
            context?.foundTooManyParameters(file, it.parameters.size, it.qualifiedName.split(".").last())
        }
    }
}