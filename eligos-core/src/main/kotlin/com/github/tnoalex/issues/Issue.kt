package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.formatter.Formatable
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.formatter.UnpackIgnore
import com.github.tnoalex.specs.FormatterSpec
import com.github.tnoalex.utils.relativePath

abstract class Issue(
    val layer: AnalysisHierarchyEnum,
    @UnpackIgnore
    val affectedFiles: HashSet<String>,
    val issueName: String,
    @UnpackIgnore
    val content: String? = null
) : Formatable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Issue

        if (layer != other.layer) return false
        if (affectedFiles != other.affectedFiles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = layer.hashCode()
        result = 31 * result + affectedFiles.hashCode()
        return result
    }

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = unpackingIssue()
        rawMap["affectedFiles"] = affectedFiles.map { relativePath(spec.srcPathPrefix, it) }
        if (spec.resultFormat == FormatterTypeEnum.HTML || spec.resultFormat == FormatterTypeEnum.TEXT) {
            content?.let {
                rawMap["content"] = it
            }
        }
        return rawMap
    }
}
