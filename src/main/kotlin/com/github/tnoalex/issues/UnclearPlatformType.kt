package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class UnclearPlatformType(
    providerFile: String,
    receiverFile: String,
    val receiverFunction: String,
    val receiverLine: Int
) : Issue(AnalysisHierarchyEnum.STRUCTURE, hashSetOf("provider@$providerFile", "receiver@$receiverFile")) {
    val providerFile: String
        get() = affectedFiles.first { it.startsWith("provider") }.split("@")[2]
    val receiverFile: String
        get() = affectedFiles.first { it.startsWith("receiver") }.split("@")[2]
}