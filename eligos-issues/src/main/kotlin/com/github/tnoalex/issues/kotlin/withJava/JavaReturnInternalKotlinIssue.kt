package com.github.tnoalex.issues.kotlin.withJava

class JavaReturnInternalKotlinIssue(
    affectedFiles: HashSet<String>,
    javaClassFqName: String,
    val javaMethodName: String,
    val startLine: Int,
    val kotlinClassFqName: String,
) : InternalExposedIssue(affectedFiles, javaClassFqName) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as JavaReturnInternalKotlinIssue

        if (javaClassFqName != other.javaClassFqName) return false
        if (javaMethodName != other.javaMethodName) return false
        if (startLine != other.startLine) return false
        if (kotlinClassFqName != other.kotlinClassFqName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + javaClassFqName.hashCode()
        result = 31 * result + javaMethodName.hashCode()
        result = 31 * result + startLine
        result = 31 * result + kotlinClassFqName.hashCode()
        return result
    }
}