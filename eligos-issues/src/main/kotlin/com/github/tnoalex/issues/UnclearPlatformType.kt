package com.github.tnoalex.issues

import com.github.tnoalex.AnalysisHierarchyEnum

class UnclearPlatformType(
    affectedFile: String,
    val propertyName: String,
    val startLine: Int,
    val upperBound: String,
    val lowerBound: String,
    val isLocal: Boolean = false,
    val isTop: Boolean = false,
    val isMember: Boolean = false
) : Issue(AnalysisHierarchyEnum.EXPRESSION, hashSetOf(affectedFile)){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UnclearPlatformType

        if (propertyName != other.propertyName) return false
        if (startLine != other.startLine) return false
        if (upperBound != other.upperBound) return false
        if (lowerBound != other.lowerBound) return false
        if (isLocal != other.isLocal) return false
        if (isTop != other.isTop) return false
        if (isMember != other.isMember) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + propertyName.hashCode()
        result = 31 * result + startLine
        result = 31 * result + upperBound.hashCode()
        result = 31 * result + lowerBound.hashCode()
        result = 31 * result + isLocal.hashCode()
        result = 31 * result + isTop.hashCode()
        result = 31 * result + isMember.hashCode()
        return result
    }
}