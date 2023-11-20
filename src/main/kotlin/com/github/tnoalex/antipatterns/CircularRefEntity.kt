package com.github.tnoalex.antipatterns

import com.github.tnoalex.analyzer.AnalysisHierarchyEnum
import com.github.tnoalex.foundation.algorithm.AdjacencyList

class CircularRefEntity(
    affectedFiles: HashSet<String>,
    val refGraph: AdjacencyList<Int>,
) : AntiPatternEntity(AntiPatternEnum.CIRCULAR_REFS, AnalysisHierarchyEnum.FILE, affectedFiles) {
    override val identifier: Any
        get() = refGraph.identifier

}