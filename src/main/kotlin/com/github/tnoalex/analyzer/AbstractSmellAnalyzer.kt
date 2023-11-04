package com.github.tnoalex.analyzer

import com.github.tnoalex.entity.enums.AnalysisHierarchyEnum
import com.github.tnoalex.entity.enums.AntiPatternEnum
import com.github.tnoalex.formatter.FormatterFactory
import com.github.tnoalex.entity.enums.FormatterTypeEnum
import com.github.tnoalex.utils.toAdjacencyList
import depends.deptypes.DependencyType
import java.io.File


abstract class AbstractSmellAnalyzer(val supportedLanguages: String) {
    protected var context: AnalyzerContext? = null
    abstract fun analyze()

    fun createAnalyticsContext(
        language: String,
        sourcePath: File,
        outputName: String?,
        outputPath: File,
        formatter: FormatterTypeEnum
    ) {
        context = AnalyzerContext(
            language,
            sourcePath,
            outputName ?: "${language}_analysis_result",
            outputPath,
            FormatterFactory.getFormatter(formatter)
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
            context!!.foundCircularReferences(it,adjacencyList.subPartOfNodes(it))
        }
    }
}