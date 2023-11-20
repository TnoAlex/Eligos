package com.github.tnoalex.antipatterns

import com.github.tnoalex.analyzer.AnalysisHierarchyEnum

class UnusedImportPatternEntity(
    affectedFiles: HashSet<String>,
    private val useFile: String
) : AntiPatternEntity(AntiPatternEnum.UNUSED_IMPORT, AnalysisHierarchyEnum.FILE, affectedFiles) {
    override val identifier: Any
        get() = useFile

    val importedFiles: List<String>
        get() = affectedFiles.filter { it != useFile }

}
