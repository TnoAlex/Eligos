package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum


class InternalExposedIssue(
    affectedFiles: HashSet<String>,
    val javaClassFqName: String,
    val kotlinClassFqName: String?,
    val kotlinInterfacesFqNames: List<String>?
) : Issue(AnalysisHierarchyEnum.CLASS, affectedFiles) {
    val isExtend: Boolean
        get() = kotlinClassFqName != null

    val isImplement: Boolean
        get() = !kotlinInterfacesFqNames.isNullOrEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as InternalExposedIssue

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