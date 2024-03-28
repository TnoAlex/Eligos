package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue

class IncomprehensibleJavaFacadeNameIssue(
    affectedFile: String,
    val javaFacadeName: String,
    val hasTopLevelProperty: Boolean,
    val hasTopLevelFunction: Boolean
) : Issue(
    EligosIssueBundle.message("issue.name.IncomprehensibleJavaFacadeNameIssue"),
    AnalysisHierarchyEnum.FILE,
    hashSetOf(affectedFile)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as IncomprehensibleJavaFacadeNameIssue

        if (javaFacadeName != other.javaFacadeName) return false
        if (hasTopLevelProperty != other.hasTopLevelProperty) return false
        if (hasTopLevelFunction != other.hasTopLevelFunction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + javaFacadeName.hashCode()
        result = 31 * result + hasTopLevelProperty.hashCode()
        result = 31 * result + hasTopLevelFunction.hashCode()
        return result
    }
}