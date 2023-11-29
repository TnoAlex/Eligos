package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class UnusedImportIssue(
    affectedFiles: HashSet<String>,
    private val useFile: String
) : Issue(AnalysisHierarchyEnum.FILE, affectedFiles) {
    override val identifier: Any
        get() = useFile

    val importedFiles: List<String>
        get() = affectedFiles.filter { it != useFile }

}
