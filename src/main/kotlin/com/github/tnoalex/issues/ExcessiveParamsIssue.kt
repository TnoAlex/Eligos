package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class ExcessiveParamsIssue(
    affectedFile: String,
    val functionName: String,
    val parameters: Map<String, String>
) : Issue(
    AnalysisHierarchyEnum.METHOD,
    hashSetOf(affectedFile)
) {
    val arity: Int get() = parameters.size

    val functionSignature: String
        get() {
            val sb = StringBuilder("$functionName(")
            parameters.forEach { (k, v) ->
                sb.append(k).append(":").append(v).append(",")
            }
            if (parameters.isNotEmpty())
                sb[sb.lastIndex] = ')'
            else
                sb.append(")")
            return sb.toString()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ExcessiveParamsIssue

        if (functionName != other.functionName) return false
        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + functionName.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }

}