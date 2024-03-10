package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class OptimizedTailRecursionIssue(
    affectedFile: String,
    functionFqName: String,
    valueParamList: List<String>,
    startLine: Int
) : FunctionTypeIssue(AnalysisHierarchyEnum.METHOD, hashSetOf(affectedFile), functionFqName, valueParamList, startLine,"Optimized Tail Recursion")
