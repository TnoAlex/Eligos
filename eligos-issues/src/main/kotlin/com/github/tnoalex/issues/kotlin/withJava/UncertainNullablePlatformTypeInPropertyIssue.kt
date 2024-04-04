package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.formatter.UnpackIgnore
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.specs.FormatterSpec

class UncertainNullablePlatformTypeInPropertyIssue(
    affectedFile: String,
    content: String,
    val propertyName: String,
    val startLine: Int,
    val upperBound: String,
    val lowerBound: String,
    @UnpackIgnore
    val isTop: Boolean = false,
    @UnpackIgnore
    val isLocal: Boolean = false,
) : Issue(
    EligosIssueBundle.message("issue.name.UncertainNullablePlatformTypeInPropertyIssue"),
    Severity.CODE_SMELL,
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile),
    content
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UncertainNullablePlatformTypeInPropertyIssue

        if (propertyName != other.propertyName) return false
        if (startLine != other.startLine) return false
        if (upperBound != other.upperBound) return false
        if (lowerBound != other.lowerBound) return false
        if (isTop != other.isTop) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + propertyName.hashCode()
        result = 31 * result + startLine
        result = 31 * result + upperBound.hashCode()
        result = 31 * result + lowerBound.hashCode()
        result = 31 * result + isTop.hashCode()
        return result
    }

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["propertyType"] = if (isTop) "topLevel" else if (isLocal) "local" else "member"
        return rawMap
    }
}