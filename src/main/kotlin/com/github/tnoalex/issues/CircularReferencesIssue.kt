package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.foundation.algorithm.AdjacencyList

class CircularReferencesIssue(
    affectedFiles: HashSet<String>,
    private val refGraph: AdjacencyList<Int>,
) : Issue(AnalysisHierarchyEnum.FILE, affectedFiles) {
    override val identifier: Any
        get() = refGraph.identifier

}