package com.github.tnoalex.antipatterns

import com.github.tnoalex.analyzer.AnalysisHierarchyEnum
import com.github.tnoalex.utils.encodeBySHA1ToString

class ExcessiveParamsEntity(
    affectedFile: String,
    val functionName: String,
    val arity: Int
) : AntiPatternEntity(
    AntiPatternEnum.TOO_MANY_PARAMS,
    AnalysisHierarchyEnum.METHOD,
    hashSetOf(affectedFile)
) {
    override val identifier by lazy {
        encodeBySHA1ToString(affectedFiles.first() + functionName + arity)
    }

}