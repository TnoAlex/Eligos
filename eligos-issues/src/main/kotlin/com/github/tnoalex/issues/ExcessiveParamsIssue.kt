package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class ExcessiveParamsIssue(
    affectedFile: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int,
    val arity: Int
) : FunctionTypeIssue(
    AnalysisHierarchyEnum.METHOD,
    hashSetOf(affectedFile), functionFqName, valueParamList, startLine
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