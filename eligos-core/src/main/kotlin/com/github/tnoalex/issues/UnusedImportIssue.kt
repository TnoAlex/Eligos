package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class UnusedImportIssue(
    affectedFiles: HashSet<String>,
    val useFile: String
) : Issue(AnalysisHierarchyEnum.FILE, affectedFiles) {

    val importedFiles: List<String>
        get() = affectedFiles.filter { it != useFile }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UnusedImportIssue

        return useFile == other.useFile
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + useFile.hashCode()
        return result
    }


}
