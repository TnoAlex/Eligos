package com.github.tnoalex.antipatterns

import com.github.tnoalex.analyzer.AnalysisHierarchyEnum
import com.github.tnoalex.utils.encodeBySHA1ToString

class LongMethodPatternEntity(
    affectedFile: String,
    val methodId: String,
    val circleComplexity: Int
) : AntiPatternEntity(
    AntiPatternEnum.LONG_METHOD, AnalysisHierarchyEnum.METHOD, hashSetOf(affectedFile)
) {
    override val identifier by lazy {
        encodeBySHA1ToString(affectedFile + methodId + circleComplexity)
    }

    val methodName: String
        get() = methodId.split("@")[0]

    val methodParamNumber: Int
        get() = methodId.split("@")[1].toInt()
}