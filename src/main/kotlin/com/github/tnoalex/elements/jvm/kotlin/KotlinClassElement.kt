package com.github.tnoalex.elements.jvm.kotlin

import com.github.tnoalex.elements.AbstractElement
import java.util.*

class KotlinClassElement(
    className: String?,
    classStartLine: Int,
    classStopLine: Int,
    parent: AbstractElement,
    annotations: LinkedList<String>,
    private val packageName: String?,
    private val type: String,
    visibilityModifier: String?,
    classModifiers: String?,
    inheritanceModifier: String?
) : KotlinElement(
    className,
    classStartLine,
    classStopLine,
    parent,
    annotations,
    visibilityModifier,
    classModifiers?.let { listOf(classModifiers) },
    inheritanceModifier
) {
    val qualifiedName: String
        get() = "$packageName.$elementName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as KotlinClassElement

        if (packageName != other.packageName) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (packageName?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }
}