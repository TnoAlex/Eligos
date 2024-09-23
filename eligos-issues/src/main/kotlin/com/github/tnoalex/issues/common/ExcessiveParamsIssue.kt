package com.github.tnoalex.issues.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.FunctionTypeIssue
import com.github.tnoalex.issues.Severity

class ExcessiveParamsIssue(
    affectedFile: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int,
    val arity: Int
) : FunctionTypeIssue(
    EligosIssueBundle.message("issue.name.ExcessiveParamsIssue"),
    Severity.CODE_SMELL,
    ConfidenceLevel.COMPLETELY_TRUSTWORTHY,
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile), null, functionFqName, valueParamList, startLine
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ExcessiveParamsIssue

        return arity == other.arity
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + arity
        return result
    }
}