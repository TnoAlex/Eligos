package com.github.tnoalex.elements.jvm.java

import com.github.tnoalex.elements.AbstractElement
import java.util.*

class JavaFunctionElement(
    functionName: String?,
    functionStartLine: Int,
    functionStopLine: Int,
    val parameters: LinkedList<JavaParameterElement>,
    parent: AbstractElement,
    annotations: LinkedList<String>,
    modifiers: LinkedList<String>
) : JavaElement(functionName, functionStartLine, functionStopLine, parent, annotations, modifiers){
    init {
        parameters.forEach {
            it.parent = this
        }
    }

    val functionSignature: String
        get() {
            val sb = StringBuilder("$elementName(")
            parameters.forEach {
                sb.append(it.toString()).append(",")
            }
            if (parameters.isNotEmpty())
                sb[sb.lastIndex] = ')'
            else
                sb.append(")")
            return sb.toString()
        }
}