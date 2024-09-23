package com.github.tnoalex.issues.kotlin.withJava.internalExpose

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.specs.FormatterSpec


sealed class InternalExposedIssue(
    affectedFiles: HashSet<String>,
    val javaClassFqName: String,
) : Issue(
    EligosIssueBundle.message("issue.name.InternalExposedIssue"),
    Severity.CODE_SMELL,
    AnalysisHierarchyEnum.CLASS,
    affectedFiles,
    null
) {

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["exposeType"] = when (this) {
            is JavaExtendOrImplInternalKotlinIssue -> "super"
            is JavaParameterInternalKotlinIssue -> "parameter"
            is JavaReturnInternalKotlinIssue -> "return"
        }
        return rawMap
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InternalExposedIssue) return false
        if (!super.equals(other)) return false

        if (javaClassFqName != other.javaClassFqName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + javaClassFqName.hashCode()
        return result
    }
}