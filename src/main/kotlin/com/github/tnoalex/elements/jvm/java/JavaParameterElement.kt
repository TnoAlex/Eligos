package com.github.tnoalex.elements.jvm.java

import com.github.tnoalex.elements.AbstractElement
import java.util.*

class JavaParameterElement(
    parameterName: String,
    parameterStartLine: Int,
    parameterStopLine: Int,
    val parameterType: String,
    parent: AbstractElement?,
    annotations: LinkedList<String>,
    private val modifier: String
) : JavaElement(parameterName, parameterStartLine, parameterStopLine, parent, annotations, LinkedList(listOf(modifier)))