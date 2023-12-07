package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.utils.encodeBySHA1ToString

class OptimizedTailRecursionIssue(
    affectedFile: String,
    val functionSignature: String
) :
    Issue(AnalysisHierarchyEnum.METHOD, hashSetOf(affectedFile)) {

    override val identifier by lazy {
        encodeBySHA1ToString(functionSignature)
    }
}
