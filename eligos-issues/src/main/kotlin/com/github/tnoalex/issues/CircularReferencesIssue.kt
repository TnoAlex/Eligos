package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge


class CircularReferencesIssue(
    affectedFiles: HashSet<String>,
    private val refGraph: Graph<String, DefaultEdge>,
) : Issue(AnalysisHierarchyEnum.FILE, affectedFiles) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as CircularReferencesIssue

        return refGraph == other.refGraph
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + refGraph.hashCode()
        return result
    }
}