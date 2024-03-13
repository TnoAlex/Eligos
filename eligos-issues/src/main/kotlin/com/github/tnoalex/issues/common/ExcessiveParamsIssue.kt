package com.github.tnoalex.issues.common

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.FunctionTypeIssue
import com.github.tnoalex.specs.FormatterSpec

class ExcessiveParamsIssue(
    affectedFile: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int,
    val arity: Int
) : FunctionTypeIssue(
    AnalysisHierarchyEnum.METHOD,
    hashSetOf(affectedFile), null, functionFqName, valueParamList, startLine, "Excessive Parameters"
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

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["arity"] = arity
        return rawMap
    }
}