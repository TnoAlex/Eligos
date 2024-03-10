package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.specs.FormatterSpec

class ComplexFunctionIssue(
    affectedFile: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int,
    val circleComplexity: Int
) : FunctionTypeIssue(
    AnalysisHierarchyEnum.METHOD, hashSetOf(affectedFile), functionFqName, valueParamList, startLine, "Complex Function"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ComplexFunctionIssue

        return circleComplexity == other.circleComplexity
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + circleComplexity
        return result
    }

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["circleComplexity"] = circleComplexity
        return rawMap
    }
}