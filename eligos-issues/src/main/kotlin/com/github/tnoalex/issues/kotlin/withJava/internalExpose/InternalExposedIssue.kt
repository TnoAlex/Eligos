package com.github.tnoalex.issues.kotlin.withJava.internalExpose

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.specs.FormatterSpec


sealed class InternalExposedIssue(
    affectedFiles: HashSet<String>, val javaClassFqName: String
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
            is JavaExtendOrImplInternalKotlinIssue ->
                if (kotlinClassFqName != null) {
                    "extend"
                } else {
                    "implement"
                }

            is JavaParameterInternalKotlinIssue -> "parameter"
            is JavaReturnInternalKotlinIssue -> "return"
        }
        return rawMap
    }
}