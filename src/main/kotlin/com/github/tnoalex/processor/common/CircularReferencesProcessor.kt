package com.github.tnoalex.processor.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.Context
import com.github.tnoalex.events.EntityRepoFinishedEvent
import com.github.tnoalex.foundation.algorithm.AdjacencyList
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.issues.CircularReferencesIssue
import com.github.tnoalex.processor.AstProcessor
import com.github.tnoalex.utils.toAdjacencyList
import java.util.*

@Component
class CircularReferencesProcessor : AstProcessor {
    private val issues = LinkedList<CircularReferencesIssue>()

    override val order: Int
        get() = Short.MAX_VALUE.toInt()

    @EventListener
    fun process(event: EntityRepoFinishedEvent) {
        findCircularReferences(event.source as Context)
        (event.source as Context).reportIssues(issues)
        issues.clear()
    }

    private fun findCircularReferences(context: Context) {
        val fileDependency = context.getDependencyMatrix(AnalysisHierarchyEnum.FILE) ?: return
        val adjacencyList = fileDependency.toAdjacencyList()
        val scc = adjacencyList.solveSCC()
        scc.filter { it.size > 1 }.forEach {
            foundCircularReferences(it, adjacencyList.subPartOfNodes(it), context)
        }
    }

    private fun foundCircularReferences(
        affectedFilesIndexes: List<Int>,
        subReference: AdjacencyList<Int>,
        context: Context
    ) {
        val affectedFiles =
            affectedFilesIndexes.map { context.getDependencyMatrix(AnalysisHierarchyEnum.FILE)!!.getNodeName(it) }
        issues.add(CircularReferencesIssue(affectedFiles.toHashSet(), subReference))
    }
}