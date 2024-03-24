package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.FunctionTypeIssue

class ImplicitSingleExprFunctionIssue(
    affectedFile: String,
    content: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int
) : FunctionTypeIssue(
    EligosIssueBundle.message("issue.name.ImplicitSingleExprFunctionIssue"),
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile),
    content,
    functionFqName,
    valueParamList,
    startLine
)