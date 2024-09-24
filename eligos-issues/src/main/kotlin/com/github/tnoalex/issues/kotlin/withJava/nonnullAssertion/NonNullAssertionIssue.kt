package com.github.tnoalex.issues.kotlin.withJava.nonnullAssertion

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity

sealed class NonNullAssertionIssue(
    issueName: String,
    affectedFile: String,
    content: String,
    confidenceLevel: ConfidenceLevel,
    val startLine: Int
) : Issue(
    issueName,
    Severity.POSSIBLE_BUG,
    confidenceLevel,
    AnalysisHierarchyEnum.EXPRESSION,
    hashSetOf(affectedFile),
    content
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NonNullAssertionIssue) return false
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