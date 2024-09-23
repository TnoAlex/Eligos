package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity

class WhenInsteadOfCascadeIfIssue(
    affectedFile: String,
    content: String,
    val cascadeDepth: Int,
    val startLine: Int
) : Issue(
    EligosIssueBundle.message("issue.name.WhenInsteadOfCascadeIfIssue"),
    Severity.CODE_SMELL,
    ConfidenceLevel.COMPLETELY_TRUSTWORTHY,
    AnalysisHierarchyEnum.EXPRESSION,
    hashSetOf(affectedFile),
    content
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as WhenInsteadOfCascadeIfIssue

        if (cascadeDepth != other.cascadeDepth) return false
        if (startLine != other.startLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + cascadeDepth
        result = 31 * result + startLine
        return result
    }
}