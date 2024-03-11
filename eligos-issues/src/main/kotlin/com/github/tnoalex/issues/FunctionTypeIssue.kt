package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.specs.FormatterSpec

abstract class FunctionTypeIssue(
    layer: AnalysisHierarchyEnum,
    affectedFiles: HashSet<String>,
    val functionFqName: String,
    val valueParamList: List<String>,
    val startLine: Int,
    issueName: String
) : Issue(layer, affectedFiles, issueName) {
    val functionSignature: String by lazy { buildSignature() }

    private fun buildSignature(): String {
        val sb = StringBuilder(functionFqName).append("(")
        valueParamList.forEach {
            sb.append(it).append(",")
        }
        return sb.removeSuffix(",").toString() + ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as FunctionTypeIssue

        if (functionFqName != other.functionFqName) return false
        if (valueParamList != other.valueParamList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + functionFqName.hashCode()
        result = 31 * result + valueParamList.hashCode()
        return result
    }

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["function"] = functionSignature
        rawMap["startLine"] = startLine
        return rawMap
    }
}