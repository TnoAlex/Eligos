package com.github.tnoalex.issues.kotlin.withJava.optional

import com.github.tnoalex.formatter.UnpackIgnore
import com.github.tnoalex.specs.FormatterSpec

class PropertyIsOptionalIssue(
    affectedFile: String,
    val propertyName: String,
    val startLine: Int,
    @UnpackIgnore
    val isTop: Boolean = false,
    @UnpackIgnore
    val isLocal: Boolean = false,
) : OptionalInKotlinIssue(affectedFile) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as PropertyIsOptionalIssue

        if (propertyName != other.propertyName) return false
        if (startLine != other.startLine) return false
        if (isTop != other.isTop) return false
        if (isLocal != other.isLocal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + propertyName.hashCode()
        result = 31 * result + startLine
        result = 31 * result + isTop.hashCode()
        result = 31 * result + isLocal.hashCode()
        return result
    }

    override fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any> {
        val rawMap = super.unwrap(spec)
        rawMap["propertyType"] = if (isTop) "topLevel" else if (isLocal) "local" else "member"
        return rawMap
    }
}