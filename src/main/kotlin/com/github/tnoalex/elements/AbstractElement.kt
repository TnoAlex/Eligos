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

    protected abstract val parent: AbstractElement?
}