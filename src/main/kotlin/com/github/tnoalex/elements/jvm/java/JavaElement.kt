package com.github.tnoalex.elements.jvm.java

import com.github.tnoalex.elements.AbstractElement
import com.github.tnoalex.elements.jvm.AbstractJvmElement
import java.util.*

open class JavaElement(
    elementName: String?,
    elementStartLine: Int,
    elementStopLine: Int,
    override var parent: AbstractElement?,
    annotations: LinkedList<String>,
    private val modifiers: LinkedList<String>?
) : AbstractJvmElement(elementName, elementStartLine, elementStopLine, annotations) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as JavaElement

        return modifiers == other.modifiers
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (modifiers?.hashCode() ?: 0)
        return result
    }
}