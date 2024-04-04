package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.EligosIssueBundle
import com.github.tnoalex.issues.FunctionTypeIssue
import com.github.tnoalex.issues.Severity

class OptimizedTailRecursionIssue(
    affectedFile: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int
) : FunctionTypeIssue(
    EligosIssueBundle.message("issue.name.OptimizedTailRecursionIssue"),
    Severity.SUGGESTION,
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile),
    null,
    functionFqName,
    valueParamList,
    startLine
)
