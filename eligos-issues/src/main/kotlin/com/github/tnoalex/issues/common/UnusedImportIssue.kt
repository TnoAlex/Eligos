package com.github.tnoalex.issues.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue

class UnusedImportIssue(
    affectedFiles: HashSet<String>,
    val unusedImports: List<String>
) : Issue(EligosIssueBundle.message("issue.name.UnusedImportIssue"), AnalysisHierarchyEnum.FILE, affectedFiles, null) {
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
