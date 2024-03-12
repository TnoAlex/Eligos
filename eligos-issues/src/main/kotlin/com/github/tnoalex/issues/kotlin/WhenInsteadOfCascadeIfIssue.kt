package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.specs.FormatterSpec

class WhenInsteadOfCascadeIfIssue(
    affectedFile: String,
    val cascadeDepth: Int,
    val startLine: Int
) : Issue(AnalysisHierarchyEnum.EXPRESSION, hashSetOf(affectedFile), "When Instead Of Cascade If") {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as WhenInsteadOfCascadeIfIssue

        if (cascadeDepth != other.cascadeDepth) return false
        if (startLine != other.startLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + cascadeDepth
        result = 31 * result + startLine
        return result
    }

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["cascadeDepth"] = cascadeDepth
        rawMap["startLine"] = startLine
        return rawMap
    }
}