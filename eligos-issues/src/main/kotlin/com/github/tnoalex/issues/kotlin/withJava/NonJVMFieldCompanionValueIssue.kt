package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue

class NonJVMFieldCompanionValueIssue(
    affectedFile: String,
    val propertyName: String,
    content: String?,
    val startLine: Int
) : Issue(
    EligosIssueBundle.message("issue.name.NonJVMFieldCompanionValueIssue"),
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile),
    content
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as NonJVMFieldCompanionValueIssue

        if (propertyName != other.propertyName) return false
        if (startLine != other.startLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + propertyName.hashCode()
        result = 31 * result + startLine
        return result
    }
}