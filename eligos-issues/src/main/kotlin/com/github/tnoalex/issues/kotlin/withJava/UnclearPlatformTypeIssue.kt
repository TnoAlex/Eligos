package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.specs.FormatterSpec

class UnclearPlatformTypeIssue(
    affectedFile: String,
    val propertyName: String,
    val startLine: Int,
    val upperBound: String,
    val lowerBound: String,
    val isLocal: Boolean = false,
    val isTop: Boolean = false,
    val isMember: Boolean = false
) : Issue(AnalysisHierarchyEnum.EXPRESSION, hashSetOf(affectedFile),"Unclear Platform Type") {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UnclearPlatformTypeIssue

        if (propertyName != other.propertyName) return false
        if (startLine != other.startLine) return false
        if (upperBound != other.upperBound) return false
        if (lowerBound != other.lowerBound) return false
        if (isLocal != other.isLocal) return false
        if (isTop != other.isTop) return false
        if (isMember != other.isMember) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + propertyName.hashCode()
        result = 31 * result + startLine
        result = 31 * result + upperBound.hashCode()
        result = 31 * result + lowerBound.hashCode()
        result = 31 * result + isLocal.hashCode()
        result = 31 * result + isTop.hashCode()
        result = 31 * result + isMember.hashCode()
        return result
    }

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["propertyName"] = propertyName
        rawMap["startLine"] = startLine
        rawMap["upperBound"] = upperBound
        rawMap["lowerBound"] = lowerBound
        rawMap["propertyType"] = if (isTop) "topLevel" else if (isLocal) "local" else "member"
        return rawMap
    }
}