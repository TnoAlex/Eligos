package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.FunctionTypeIssue
import com.github.tnoalex.issues.Severity

class ImplicitSingleExprFunctionIssue(
    affectedFile: String,
    content: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int
) : FunctionTypeIssue(
    EligosIssueBundle.message("issue.name.ImplicitSingleExprFunctionIssue"),
    Severity.CODE_SMELL,
    ConfidenceLevel.COMPLETELY_TRUSTWORTHY,
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile),
    content,
    functionFqName,
    valueParamList,
    startLine
)