package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue

class ObjectExtendsThrowableIssue(affectedFile: String, val objectFqName: String) :
    Issue(
        EligosIssueBundle.message("issue.name.ObjectExtendsThrowableIssue"),
        AnalysisHierarchyEnum.CLASS,
        hashSetOf(affectedFile),
        null
    ) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ObjectExtendsThrowableIssue

        return objectFqName == other.objectFqName
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + objectFqName.hashCode()
        return result
    }
}