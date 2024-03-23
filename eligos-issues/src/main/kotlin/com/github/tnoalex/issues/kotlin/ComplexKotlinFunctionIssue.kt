package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.FunctionTypeIssue
import com.github.tnoalex.specs.FormatterSpec

class ComplexKotlinFunctionIssue(
    affectedFile: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int,
    val circleComplexity: Int
) : FunctionTypeIssue(
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile),
    null,
    functionFqName,
    valueParamList,
    startLine
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ComplexKotlinFunctionIssue

        return circleComplexity == other.circleComplexity
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + circleComplexity
        return result
    }
}