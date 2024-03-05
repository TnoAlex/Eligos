package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum


class ImproperInternalExposedIssue(
    affectedFiles: HashSet<String>,
    val javaClassElement: String,
    val kotlinClassElement: String,
    val relation: String
) : Issue(AnalysisHierarchyEnum.CLASS, affectedFiles) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + javaClassElement.hashCode()
        result = 31 * result + kotlinClassElement.hashCode()
        return result
    }
}