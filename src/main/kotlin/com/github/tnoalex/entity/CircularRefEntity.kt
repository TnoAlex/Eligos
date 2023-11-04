package com.github.tnoalex.entity

import com.github.tnoalex.algorithm.AdjacencyList
import com.github.tnoalex.entity.enums.AnalysisHierarchyEnum
import com.github.tnoalex.entity.enums.AntiPatternEnum

class CircularRefEntity(
    affectedFiles: HashSet<String>,
    val refGraph: AdjacencyList<Int>,
) : AntiPatternEntity(AntiPatternEnum.CIRCULAR_REFS, AnalysisHierarchyEnum.FILE, affectedFiles) {
    override val identifier: Any
        get() = refGraph.identifier

}