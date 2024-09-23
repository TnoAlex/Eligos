package com.github.tnoalex.issues.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.formatter.UnpackIgnore
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.specs.FormatterSpec
import com.github.tnoalex.utils.ReferencesMatrix
import com.github.tnoalex.utils.relativePath
import com.github.tnoalex.utils.toAdjacencyMatrices
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge


class CircularReferencesIssue(
    affectedFiles: HashSet<String>,
    @UnpackIgnore
    private val refGraph: Graph<String, DefaultEdge>,
) : Issue(
    EligosIssueBundle.message("issue.name.CircularReferencesIssue"),
    Severity.CODE_SMELL,
    ConfidenceLevel.COMPLETELY_TRUSTWORTHY,
    AnalysisHierarchyEnum.FILE,
    affectedFiles,
    null
) {
    val refMatrix: ReferencesMatrix<String>
        get() = refGraph.toAdjacencyMatrices()

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
        val (node, matrix) = refMatrix
        val nodeMap = HashMap<Int, String>()
        node.forEach { (k, v) ->
            nodeMap[v] = relativePath(spec.srcPathPrefix, k)
        }
        if (spec.resultFormat == FormatterTypeEnum.JSON || spec.resultFormat == FormatterTypeEnum.HTML) {
            rawMap[::refGraph.name] = mapOf("nodeMap" to nodeMap, "matrix" to matrix)
        } else {
            val sb = StringBuilder()
            matrix.forEach {
                it.forEach { v ->
                    sb.append(v).append(" ")
                }
                sb.append("\n")
            }
            rawMap[::refGraph.name] = mapOf("nodeMap" to nodeMap, "matrix" to sb.toString())
        }
        return rawMap
    }
}