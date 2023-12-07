package com.github.tnoalex.elements.jvm.java

import com.github.tnoalex.elements.AbstractElement
import java.util.*

class JavaClassElement(
    className: String?,
    classStartLine: Int,
    classStopLine: Int,
    parent: AbstractElement,
    annotations: LinkedList<String>,
    modifiers: LinkedList<String>,
    private val packageName: String?,
) : JavaElement(className, classStartLine, classStopLine, parent, annotations, modifiers){
    val qualifiedName: String
        get() = "$packageName.$elementName"
}