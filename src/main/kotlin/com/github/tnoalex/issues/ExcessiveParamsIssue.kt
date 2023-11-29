package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.utils.encodeBySHA1ToString

class ExcessiveParamsIssue(
    affectedFile: String,
    val functionName: String,
    val arity: Int
) : Issue(
    AnalysisHierarchyEnum.METHOD,
    hashSetOf(affectedFile)
) {
    override val identifier by lazy {
        encodeBySHA1ToString(affectedFiles.first() + functionName + arity)
    }

}