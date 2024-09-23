package com.github.tnoalex.issues.kotlin.withJava.optional

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.specs.FormatterSpec

sealed class OptionalInKotlinIssue(
    affectedFile: String
) : Issue(
    EligosIssueBundle.message("issue.name.OptionalInKotlinIssue"),
    Severity.CODE_SMELL,
    ConfidenceLevel.COMPLETELY_TRUSTWORTHY,
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile),
    null
) {
    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["position"] = when (this) {
            is ParameterOptionalIssue -> "parameter"
            is PropertyIsOptionalIssue -> "property"
            is ReturnOptionalIssue -> "return"
        }
        return rawMap
    }
}