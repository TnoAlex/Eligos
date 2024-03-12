package com.github.tnoalex.issues.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.specs.FormatterSpec
import com.github.tnoalex.utils.relativePath
import com.github.tnoalex.utils.toAdjacencyMatrices
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge


class CircularReferencesIssue(
    affectedFiles: HashSet<String>,
    private val refGraph: Graph<String, DefaultEdge>,
) : Issue(AnalysisHierarchyEnum.FILE, affectedFiles, "Circular References") {
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

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        val (node, matrix) = refGraph.toAdjacencyMatrices()
        val nodeMap = HashMap<Int, String>()
        node.forEach { (k, v) ->
            nodeMap[v] = relativePath(spec.srcPathPrefix, k)
        }
        rawMap["refGraph"] = mapOf("nodeMap" to nodeMap, "graph" to matrix)
        return rawMap
    }
}