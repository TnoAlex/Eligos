package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class ImplicitSingleExprFunctionIssue(
    affectedFile: String,
    val functionSignature: String
) : Issue(AnalysisHierarchyEnum.METHOD, hashSetOf(affectedFile)) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ImplicitSingleExprFunctionIssue

        return functionSignature == other.functionSignature
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + functionSignature.hashCode()
        return result
    }
}