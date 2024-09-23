package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.formatter.Formatable
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.formatter.UnpackIgnore
import com.github.tnoalex.specs.FormatterSpec
import com.github.tnoalex.utils.relativePath

abstract class Issue(
    @UnpackIgnore
    val issueName: String,
    @UnpackIgnore
    val severity: Severity,
    val confidenceLevel: ConfidenceLevel,
    val layer: AnalysisHierarchyEnum,
    @UnpackIgnore
    val affectedFiles: HashSet<String>,
    @UnpackIgnore
    val content: String? = null
) : Formatable {
    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = LinkedHashMap<String, Any>()
        rawMap["issueName"] = issueName
        rawMap["severity"] = severity.describe
        rawMap.putAll(unpackingIssue())
        rawMap["affectedFiles"] = affectedFiles.map { relativePath(spec.srcPathPrefix, it) }
        if (spec.resultFormat == FormatterTypeEnum.HTML || spec.resultFormat == FormatterTypeEnum.TEXT) {
            content?.let {
                rawMap["content"] = it
            }
        }
        return rawMap
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Issue

        if (issueName != other.issueName) return false
        if (layer != other.layer) return false
        if (affectedFiles != other.affectedFiles) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        var result = issueName.hashCode()
        result = 31 * result + layer.hashCode()
        result = 31 * result + affectedFiles.hashCode()
        result = 31 * result + (content?.hashCode() ?: 0)
        return result
    }
}
