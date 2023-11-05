package com.github.tnoalex.entity.antipatterns

import com.github.tnoalex.entity.enums.AnalysisHierarchyEnum
import com.github.tnoalex.entity.enums.AntiPatternEnum

abstract class AntiPatternEntity(
    val type: AntiPatternEnum,
    val layer: AnalysisHierarchyEnum,
    val affectedFiles: HashSet<String>
) {
    // Use this unique identifier to identify different entities
    abstract val identifier: Any
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is AntiPatternEntity) return false
        return other.identifier == identifier
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }
}
