package com.github.tnoalex.issues.kotlin.withJava.internalExpose

class JavaExtendOrImplInternalKotlinIssue(
    affectedFiles: HashSet<String>,
    javaClassFqName: String,
    val exposedTypes: HashSet<String>,
) : InternalExposedIssue(affectedFiles, javaClassFqName) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JavaExtendOrImplInternalKotlinIssue) return false
        if (!super.equals(other)) return false

        if (exposedTypes != other.exposedTypes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + exposedTypes.hashCode()
        return result
    }
}