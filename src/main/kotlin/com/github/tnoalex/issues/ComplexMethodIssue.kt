package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.utils.encodeBySHA1ToString

class ComplexMethodIssue(
    affectedFile: String,
    val methodSignature: String,
    val circleComplexity: Int
) : Issue(
    AnalysisHierarchyEnum.METHOD, hashSetOf(affectedFile)
) {
    override val identifier by lazy {
        encodeBySHA1ToString(affectedFile + methodSignature + circleComplexity)
    }

    val methodName: String
        get() = methodSignature.split("(")[0]
}