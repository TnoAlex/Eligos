package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

abstract class Issue(
    val layer: AnalysisHierarchyEnum,
    val affectedFiles: HashSet<String>
) {
    // Use this unique identifier to identify different entities
    abstract val identifier: Any
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Issue) return false
        return other.identifier == identifier
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }
}
