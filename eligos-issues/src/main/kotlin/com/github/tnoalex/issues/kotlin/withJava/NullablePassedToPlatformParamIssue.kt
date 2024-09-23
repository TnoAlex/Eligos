package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity

class NullablePassedToPlatformParamIssue(
    affectedFile: String,
    content: String,
    val startLine: Int,
    val calledFunctionFile: String,
    val calledFunctionName: String,
    val calledFunctionStartLine: Int,
    val parameterIndex: Int
) : Issue(
    EligosIssueBundle.message("issue.name.NullablePassedToPlatformParamIssue"),
    Severity.POSSIBLE_BUG,
    ConfidenceLevel.MEDIUM,
    AnalysisHierarchyEnum.EXPRESSION,
    hashSetOf(affectedFile),
    content
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as NullablePassedToPlatformParamIssue

        if (startLine != other.startLine) return false
        if (calledFunctionFile != other.calledFunctionFile) return false
        if (calledFunctionName != other.calledFunctionName) return false
        if (calledFunctionStartLine != other.calledFunctionStartLine) return false
        if (parameterIndex != other.parameterIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + startLine
        result = 31 * result + calledFunctionFile.hashCode()
        result = 31 * result + calledFunctionName.hashCode()
        result = 31 * result + calledFunctionStartLine
        result = 31 * result + parameterIndex
        return result
    }

}