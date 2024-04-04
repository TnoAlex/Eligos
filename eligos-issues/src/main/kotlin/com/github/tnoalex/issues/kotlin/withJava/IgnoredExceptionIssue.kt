package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity

@Suppress("unused")
class IgnoredExceptionIssue(
    affectedFile: String,
    content: String?,
    val ignoredExceptions: String,
    val startLine: Int,
    var calledByJava: Boolean
) : Issue(
    EligosIssueBundle.message("issue.name.IgnoredExceptionIssue"),
    Severity.CODE_SMELL,
    AnalysisHierarchyEnum.EXPRESSION,
    hashSetOf(affectedFile),
    content
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as IgnoredExceptionIssue

        if (ignoredExceptions != other.ignoredExceptions) return false
        if (startLine != other.startLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + ignoredExceptions.hashCode()
        result = 31 * result + startLine
        return result
    }
}