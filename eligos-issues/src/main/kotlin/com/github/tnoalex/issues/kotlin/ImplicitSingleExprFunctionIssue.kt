package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.FunctionTypeIssue

class ImplicitSingleExprFunctionIssue(
    affectedFile: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int
) : FunctionTypeIssue(AnalysisHierarchyEnum.METHOD, hashSetOf(affectedFile), functionFqName, valueParamList, startLine,"Implicit Single Expression Function")