package com.github.tnoalex.issues.kotlin.withJava.internalExpose

class JavaExtendOrImplInternalKotlinIssue(
    affectedFiles: HashSet<String>,
    javaClassFqName: String,
    val kotlinClassFqName: String?,
    val kotlinInterfacesFqNames: List<String>?
) : InternalExposedIssue(affectedFiles, javaClassFqName) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as JavaExtendOrImplInternalKotlinIssue

        if (javaClassFqName != other.javaClassFqName) return false
        if (kotlinClassFqName != other.kotlinClassFqName) return false
        if (kotlinInterfacesFqNames != other.kotlinInterfacesFqNames) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + javaClassFqName.hashCode()
        result = 31 * result + (kotlinClassFqName?.hashCode() ?: 0)
        result = 31 * result + (kotlinInterfacesFqNames?.hashCode() ?: 0)
        return result
    }
}