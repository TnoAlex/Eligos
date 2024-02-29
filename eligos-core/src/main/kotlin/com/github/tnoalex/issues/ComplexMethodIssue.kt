package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class ComplexMethodIssue(
    affectedFile: String,
    val methodSignature: String,
    val circleComplexity: Int
) : Issue(
    AnalysisHierarchyEnum.METHOD, hashSetOf(affectedFile)
) {
    val methodName: String
        get() = methodSignature.split("(")[0]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ComplexMethodIssue

        if (methodSignature != other.methodSignature) return false
        if (circleComplexity != other.circleComplexity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + methodSignature.hashCode()
        result = 31 * result + circleComplexity
        return result
    }
}