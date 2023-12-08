package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

abstract class Issue(
    val layer: AnalysisHierarchyEnum,
    val affectedFiles: HashSet<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Issue

        if (layer != other.layer) return false
        if (affectedFiles != other.affectedFiles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = layer.hashCode()
        result = 31 * result + affectedFiles.hashCode()
        return result
    }
}
