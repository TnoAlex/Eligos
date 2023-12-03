package com.github.tnoalex.elements.kotlin

import com.github.tnoalex.elements.AbstractElement

data class KotlinFunctionElement(
    val functionName: String,
    val functionStartLine: Int,
    val functionStopLine: Int,
    override val parent: AbstractElement,
    private val parameterNumber: Int,
    private val visibility: String?,
    private val functionModifier: String?,
    private val inheritanceModifier: String?
) : AbstractElement(functionName, functionStartLine, functionStopLine) {
    val functionId: String
        get() = "$functionName@$parameterNumber"

    val isTopLevel: Boolean
        get() = parent is KotlinFunctionElement
}