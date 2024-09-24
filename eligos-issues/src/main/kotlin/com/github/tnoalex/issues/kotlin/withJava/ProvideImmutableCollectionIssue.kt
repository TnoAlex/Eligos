package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.*

@Suppress("unused")
class ProvideImmutableCollectionIssue(
    affectedFiles: HashSet<String>,
    val providerKtElementFqName: String,
    val isFunction: Boolean,
    val isProperty: Boolean,
    val startLine: Int,
    content: String,
    val useJavaClassFqName: String
) : Issue(
    EligosIssueBundle.message("issue.name.ProvideImmutableCollectionIssue"),
    Severity.POSSIBLE_BUG,
    normal,
    AnalysisHierarchyEnum.EXPRESSION,
    affectedFiles,
    content
) {
    companion object {
        @JvmStatic
        val normal: ConfidenceLevel = ConfidenceLevel.MEDIUM
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ProvideImmutableCollectionIssue

        if (providerKtElementFqName != other.providerKtElementFqName) return false
        if (startLine != other.startLine) return false
        if (useJavaClassFqName != other.useJavaClassFqName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + providerKtElementFqName.hashCode()
        result = 31 * result + startLine
        result = 31 * result + useJavaClassFqName.hashCode()
        return result
    }
}