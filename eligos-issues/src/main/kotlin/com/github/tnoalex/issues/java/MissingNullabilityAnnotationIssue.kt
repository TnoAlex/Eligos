package com.github.tnoalex.issues.java

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity

class MissingNullabilityAnnotationIssue(
    affectedFile: String,
    val classFqName: String?,
    val functionName: String,
    val startLine: Int,
): Issue(
    EligosIssueBundle.message("issue.name.MissingNullabilityAnnotation"),
    Severity.SUGGESTION,
    ConfidenceLevel.LOW,
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MissingNullabilityAnnotationIssue) return false
        if (!super.equals(other)) return false

        if (classFqName != other.classFqName) return false
        if (functionName != other.functionName) return false
        if (startLine != other.startLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (classFqName?.hashCode() ?: 0)
        result = 31 * result + functionName.hashCode()
        result = 31 * result + startLine
        return result
    }
}