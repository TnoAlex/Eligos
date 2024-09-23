package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.formatter.UnpackIgnore

@Suppress("unused")
abstract class FunctionTypeIssue(
    issueName: String,
    severity: Severity,
    confidenceLevel: ConfidenceLevel,
    layer: AnalysisHierarchyEnum,
    affectedFiles: HashSet<String>,
    content: String?,
    @UnpackIgnore
    val functionFqName: String,
    val valueParamList: List<String>?,
    val startLine: Int
) : Issue(issueName, severity, confidenceLevel, layer, affectedFiles, content) {
    val functionSignature: String by lazy { buildSignature() }

    private fun buildSignature(): String {
        val sb = StringBuilder(functionFqName).append("(")
        valueParamList?.forEach {
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
}