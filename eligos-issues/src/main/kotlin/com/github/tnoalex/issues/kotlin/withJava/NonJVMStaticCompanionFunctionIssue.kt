package com.github.tnoalex.issues.kotlin.withJava

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.FunctionTypeIssue
import com.github.tnoalex.issues.Severity

class NonJVMStaticCompanionFunctionIssue(
    affectedFile: String,
    functionFqName: String,
    valueParamList: List<String>?,
    startLine: Int,
    content: String?
) : FunctionTypeIssue(
    EligosIssueBundle.message("issue.name.NonJVMStaticCompanionFunctionIssue"),
    Severity.SUGGESTION,
    ConfidenceLevel.COMPLETELY_TRUSTWORTHY,
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile),
    content,
    functionFqName,
    valueParamList,
    startLine
)