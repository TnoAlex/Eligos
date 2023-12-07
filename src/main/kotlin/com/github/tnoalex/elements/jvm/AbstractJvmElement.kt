package com.github.tnoalex.elements.jvm

import com.github.tnoalex.elements.AbstractElement
import java.util.*

abstract class AbstractJvmElement(
    elementName: String?,
    elementStartLine: Int,
    elementStopLine: Int,
    override var parent: AbstractElement?,
    protected val annotations: LinkedList<String>
) : AbstractElement(elementName, elementStartLine, elementStopLine){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as AbstractJvmElement

        return annotations == other.annotations
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + annotations.hashCode()
        return result
    }
}