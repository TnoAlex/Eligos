package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.specs.FormatterSpec

class IgnoreExceptionIssue(
    affectedFile: String,
    content: String?,
    val ignoredExceptions: String,
    val startLine: Int
) : Issue(AnalysisHierarchyEnum.EXPRESSION, hashSetOf(affectedFile), "Ignore Exception", content) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as IgnoreExceptionIssue

        if (ignoredExceptions != other.ignoredExceptions) return false
        if (startLine != other.startLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + ignoredExceptions.hashCode()
        result = 31 * result + startLine
        return result
    }

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["ignoredExceptions"] = ignoredExceptions
        rawMap["startLine"] = startLine
        return rawMap
    }
}