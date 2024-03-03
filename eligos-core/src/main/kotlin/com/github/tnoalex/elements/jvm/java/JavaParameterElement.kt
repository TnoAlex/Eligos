package com.github.tnoalex.elements.jvm.java

import com.github.tnoalex.elements.AbstractElement
import java.util.*

class JavaParameterElement(
    parameterName: String,
    parameterStartLine: Int,
    parameterStopLine: Int,
    val parameterType: String,
    val lastParameter: Boolean,
    parent: AbstractElement?,
    annotations: LinkedList<String>,
    modifier: String?
) : JavaElement(
    parameterName,
    parameterStartLine,
    parameterStopLine,
    parent,
    annotations,
    modifier?.let { LinkedList(listOf(it)) })