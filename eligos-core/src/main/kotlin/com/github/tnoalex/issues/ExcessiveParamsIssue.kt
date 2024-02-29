package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class ExcessiveParamsIssue(
    affectedFile: String,
    val functionName: String,
    val arity: Int
) : Issue(
    AnalysisHierarchyEnum.METHOD,
    hashSetOf(affectedFile)
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ExcessiveParamsIssue

        return functionName == other.functionName
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + functionName.hashCode()
        return result
    }

}