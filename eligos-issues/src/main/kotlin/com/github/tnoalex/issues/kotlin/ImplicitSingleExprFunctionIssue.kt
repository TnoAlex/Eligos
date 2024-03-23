package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.FunctionTypeIssue

class ImplicitSingleExprFunctionIssue(
    affectedFile: String,
    content: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int
) : FunctionTypeIssue(
    AnalysisHierarchyEnum.MEMBER,
    hashSetOf(affectedFile),
    content,
    functionFqName,
    valueParamList,
    startLine
)