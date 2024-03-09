package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class ProvideImmutableCollectionIssue(
    affectedFiles: HashSet<String>,
    val providerKtFunFqName: String,
    val startLine: Int,
    val useJavaClassFqName: String
) : Issue(AnalysisHierarchyEnum.EXPRESSION, affectedFiles) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ProvideImmutableCollectionIssue

        if (providerKtFunFqName != other.providerKtFunFqName) return false
        if (startLine != other.startLine) return false
        if (useJavaClassFqName != other.useJavaClassFqName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + providerKtFunFqName.hashCode()
        result = 31 * result + startLine
        result = 31 * result + useJavaClassFqName.hashCode()
        return result
    }
}