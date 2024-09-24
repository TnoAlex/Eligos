package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.*


class UncertainNullablePlatformCallerIssue(
    affectedFile: String,
    content: String,
    val startLine: Int,
    val smartCastedActualType: String,
    val caller: String
) : Issue(
    EligosIssueBundle.message("issue.name.UncertainNullablePlatformCallerIssue"),
    Severity.POSSIBLE_BUG,
    normal,
    AnalysisHierarchyEnum.EXPRESSION,
    hashSetOf(affectedFile),
    content
) {
    companion object {
        @JvmStatic
        val normal: ConfidenceLevel = ConfidenceLevel.MEDIUM
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UncertainNullablePlatformCallerIssue

        if (startLine != other.startLine) return false
        if (smartCastedActualType != other.smartCastedActualType) return false
        if (caller != other.caller) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + startLine
        result = 31 * result + smartCastedActualType.hashCode()
        result = 31 * result + caller.hashCode()
        return result
    }
}