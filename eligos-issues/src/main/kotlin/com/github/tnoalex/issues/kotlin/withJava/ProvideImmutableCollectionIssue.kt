package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.specs.FormatterSpec

class ProvideImmutableCollectionIssue(
    affectedFiles: HashSet<String>,
    val providerKtElementFqName: String,
    val isFunction: Boolean,
    val isProperty: Boolean,
    val startLine: Int,
    content: String,
    val useJavaClassFqName: String
) : Issue(AnalysisHierarchyEnum.EXPRESSION, affectedFiles, "Provide Immutable Collection", content) {
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

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["useJavaClassFqName"] = useJavaClassFqName
        rawMap["providerKtElementFqName"] = providerKtElementFqName
        if (isFunction) rawMap["isFunction"] = true else rawMap["isProperty"] = true
        rawMap["startLine"] = startLine
        return rawMap
    }
}