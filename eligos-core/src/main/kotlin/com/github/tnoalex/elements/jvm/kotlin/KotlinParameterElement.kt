package com.github.tnoalex.elements.jvm.kotlin

import com.github.tnoalex.elements.AbstractElement
import com.github.tnoalex.elements.jvm.kotlin.KotlinElement
import java.util.*

class KotlinParameterElement(
    parameterName: String,
    parameterStartLine: Int,
    parameterStopLine: Int,
    val parameterType: String,
    parent: AbstractElement?,
    annotations: LinkedList<String>,
    parameterModifier: List<String>?
) : KotlinElement(
    parameterName,
    parameterStartLine,
    parameterStopLine,
    parent,
    annotations,
    null,
    parameterModifier,
    null
) {
    override fun toString(): String {
        return "$elementName:$parameterType"
    }
}