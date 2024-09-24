package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.*


class UncertainNullablePlatformExpressionUsageIssue(
    affectedFile: String,
    content: String,
    val startLine: Int,
    val expectType: String,
    val actualType: String,
    val smartCastedActualType: String,
    confidenceLevel: ConfidenceLevel = normal
) : Issue(
    EligosIssueBundle.message("issue.name.UncertainNullablePlatformExpressionUsageIssue"),
    Severity.POSSIBLE_BUG,
    confidenceLevel,
    AnalysisHierarchyEnum.EXPRESSION,
    hashSetOf(affectedFile),
    content
) {
    companion object {
        @JvmStatic
        val cast: ConfidenceLevel = ConfidenceLevel.LOW
        @JvmStatic
        val normal: ConfidenceLevel = ConfidenceLevel.MEDIUM
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UncertainNullablePlatformExpressionUsageIssue

        if (startLine != other.startLine) return false
        if (expectType != other.expectType) return false
        if (actualType != other.actualType) return false
        if (smartCastedActualType != other.smartCastedActualType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + startLine
        result = 31 * result + expectType.hashCode()
        result = 31 * result + actualType.hashCode()
        result = 31 * result + smartCastedActualType.hashCode()
        return result
    }
}