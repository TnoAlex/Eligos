package com.github.tnoalex.analyzer

import com.github.tnoalex.entity.enums.AnalysisHierarchyEnum
import com.github.tnoalex.entity.enums.FormatterTypeEnum
import com.github.tnoalex.formatter.FormatterFactory
import com.github.tnoalex.foundation.common.LanguageSupportInfo
import com.github.tnoalex.rules.FunctionRule
import com.github.tnoalex.rules.RuleContainer
import com.github.tnoalex.utils.getEntitiesByType
import com.github.tnoalex.utils.toAdjacencyList
import depends.deptypes.DependencyType
import depends.entity.FunctionEntity


abstract class AbstractSmellAnalyzer : LanguageSupportInfo {
    protected var context: AnalyzerContext? = null
    abstract fun analyze()

    fun createAnalyticsContext(
        language: String,
        formatter: FormatterTypeEnum
    ) {
        context = AnalyzerContext(
            language,
            FormatterFactory.getFormatter(formatter) ?: throw RuntimeException("Unknown result formatter")
        )
    }

    fun findUselessImport() {
        val fileDependency = context!!.getDependencyMatrix(AnalysisHierarchyEnum.FILE)
        fileDependency?.dependencyPairs?.filter {
            it.dependencies.size == 1 && it.dependencies.first().type.equals(DependencyType.IMPORT)
        }.orEmpty().forEach {
            context!!.foundUnusedImportPattern(listOf(it.from, it.to))
        }
    }

    fun findCircularReferences() {
        val fileDependency = context!!.getDependencyMatrix(AnalysisHierarchyEnum.FILE) ?: return
        val adjacencyList = fileDependency.toAdjacencyList()
        val scc = adjacencyList.solveSCC()
        scc.filter { it.size > 1 }.forEach {
            context!!.foundCircularReferences(it, adjacencyList.subPartOfNodes(it))
        }
    }

    fun findTooManyParameters() {
        val functionEntities = context!!.entityRepo?.getEntitiesByType(FunctionEntity::class.java) ?: return
        val functionDependency = context!!.getDependencyMatrix(AnalysisHierarchyEnum.METHOD)
        functionEntities.map { it as FunctionEntity }.filter {
            it.parameters.size > (RuleContainer.INSTANT.getRuleByType(FunctionRule::class) as FunctionRule).arity
        }.forEach {
            val file = functionDependency!!.nodes.first { f ->
                f.split("(")[1] == it.qualifiedName + ")"
            }.split("(")[0]
            context!!.foundTooManyParameters(file, it.parameters.size, it.qualifiedName.split(".").last())
        }
    }
}