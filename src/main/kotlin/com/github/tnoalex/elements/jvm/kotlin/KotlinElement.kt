package com.github.tnoalex.elements.jvm.kotlin

import com.github.tnoalex.elements.AbstractElement
import com.github.tnoalex.elements.jvm.AbstractJvmElement
import java.util.*

open class KotlinElement(
    elementName: String?,
    elementStartLine: Int,
    elementStopLine: Int,
    parent: AbstractElement?,
    annotations: LinkedList<String>,
    private val visibilityModifier: String?,
    protected val elementModifier: List<String>?,
    private val inheritanceModifier: String?
) : AbstractJvmElement(elementName, elementStartLine, elementStopLine, parent, annotations) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as KotlinElement

        if (visibilityModifier != other.visibilityModifier) return false
        if (elementModifier != other.elementModifier) return false
        if (inheritanceModifier != other.inheritanceModifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (visibilityModifier?.hashCode() ?: 0)
        result = 31 * result + (elementModifier?.hashCode() ?: 0)
        result = 31 * result + (inheritanceModifier?.hashCode() ?: 0)
        return result
    }
}