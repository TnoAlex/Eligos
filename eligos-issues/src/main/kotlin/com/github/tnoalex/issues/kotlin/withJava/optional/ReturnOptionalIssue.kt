package com.github.tnoalex.issues.kotlin.withJava.optional

class ReturnOptionalIssue(
    affectedFile: String,
    val classFqName: String?,
    val functionName: String,
    val startLine: Int,
) : OptionalInKotlinIssue(affectedFile) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ReturnOptionalIssue

        if (classFqName != other.classFqName) return false
        if (functionName != other.functionName) return false
        if (startLine != other.startLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (classFqName?.hashCode() ?: 0)
        result = 31 * result + functionName.hashCode()
        result = 31 * result + startLine
        return result
    }
}