package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class UnusedImportIssue(
    affectedFiles: HashSet<String>,
    val unusedImports: List<String>
) : Issue(AnalysisHierarchyEnum.FILE, affectedFiles) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UnusedImportIssue

        return unusedImports == other.unusedImports
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + unusedImports.hashCode()
        return result
    }
}
