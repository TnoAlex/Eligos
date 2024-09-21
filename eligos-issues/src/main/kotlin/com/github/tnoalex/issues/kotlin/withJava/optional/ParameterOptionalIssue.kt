package com.github.tnoalex.issues.kotlin.withJava.optional

class ParameterOptionalIssue(
    affectedFile: String,
    val classFqName: String?,
    val functionName: String,
    val startLine: Int,
    val parameterIndices: List<Int>,
) : OptionalInKotlinIssue(affectedFile) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ParameterOptionalIssue

        if (classFqName != other.classFqName) return false
        if (functionName != other.functionName) return false
        if (startLine != other.startLine) return false
        if (parameterIndices != other.parameterIndices) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (classFqName?.hashCode() ?: 0)
        result = 31 * result + functionName.hashCode()
        result = 31 * result + startLine
        result = 31 * result + parameterIndices.hashCode()
        return result
    }
}