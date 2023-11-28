package com.github.tnoalex.elements

import java.util.*

abstract class AbstractElement {
    abstract val elementName: String?
    protected abstract val elementStartLine: Int
    protected abstract val elementStopLine: Int

    val lineNumber: Int
        get() = elementStopLine - elementStartLine + 1

    val innerElement = LinkedList<AbstractElement>()

    abstract val parent: AbstractElement?
}