package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity

class NonNullAssertionOnPlatformTypeIssue(
    affectedFile: String,
    content: String,
    val startLine: Int
): Issue(
    EligosIssueBundle.message("issue.name.NonNullAssertionOnPlatformTypeIssue"),
    Severity.POSSIBLE_BUG,
    ConfidenceLevel.LOW,
    AnalysisHierarchyEnum.EXPRESSION,
    hashSetOf(affectedFile),
    content
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NonNullAssertionOnPlatformTypeIssue) return false
        if (!super.equals(other)) return false

        if (startLine != other.startLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + startLine
        return result
    }
}