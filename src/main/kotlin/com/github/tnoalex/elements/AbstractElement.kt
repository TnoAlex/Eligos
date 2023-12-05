package com.github.tnoalex.elements

import java.util.*

abstract class AbstractElement(
    val elementName: String?,
    private val elementStartLine: Int,
    private val elementStopLine: Int
) {

    val lineNumber: Int
        get() = elementStopLine - elementStartLine + 1

    val innerElement = LinkedList<AbstractElement>()

    abstract var parent: AbstractElement?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractElement

        if (elementName != other.elementName) return false
        if (lineNumber != other.lineNumber) return false
        if (innerElement != other.innerElement) return false
        return true
    }

    override fun hashCode(): Int {
        var result = elementName?.hashCode() ?: 0
        result = 31 * result + elementStartLine
        result = 31 * result + elementStopLine
        result = 31 * result + innerElement.hashCode()
        return result
    }
}