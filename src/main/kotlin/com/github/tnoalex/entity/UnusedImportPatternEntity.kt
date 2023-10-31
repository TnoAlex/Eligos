package com.github.tnoalex.entity

import com.github.tnoalex.entity.enums.AnalysisHierarchyEnum
import com.github.tnoalex.entity.enums.AntiPatternEnum

class UnusedImportPatternEntity(
    affectedFiles: HashSet<String>,
    private val useFile: String
) : AntiPatternEntity(AntiPatternEnum.UNUSED_IMPORT, AnalysisHierarchyEnum.FILE, affectedFiles) {
    override val identifier: Any
        get() = useFile

    val importedFiles: List<String>
        get() = affectedFiles.filter { it != useFile }

}
