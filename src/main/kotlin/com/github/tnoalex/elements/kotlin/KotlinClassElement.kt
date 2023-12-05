package com.github.tnoalex.elements.kotlin

import com.github.tnoalex.elements.AbstractElement

class KotlinClassElement(
    className: String?,
    classStartLine: Int,
    classStopLine: Int,
    override var parent: AbstractElement?,
    private val packageName: String?,
    private val type: String,
    private val modifiers: String?
) : AbstractElement(className, classStartLine, classStopLine) {
    val qualifiedName: String
        get() = "$packageName.$elementName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as KotlinClassElement

        if (parent != other.parent) return false
        if (packageName != other.packageName) return false
        if (type != other.type) return false
        if (modifiers != other.modifiers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + (packageName?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        result = 31 * result + (modifiers?.hashCode() ?: 0)
        return result
    }
}