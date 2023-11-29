package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.utils.encodeBySHA1ToString

class LongMethodPatternIssue(
    affectedFile: String,
    val methodId: String,
    val circleComplexity: Int
) : Issue(
    AnalysisHierarchyEnum.METHOD, hashSetOf(affectedFile)
) {
    override val identifier by lazy {
        encodeBySHA1ToString(affectedFile + methodId + circleComplexity)
    }

    val methodName: String
        get() = methodId.split("@")[0]

    val methodParamNumber: Int
        get() = methodId.split("@")[1].toInt()
}