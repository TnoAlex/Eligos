package com.github.tnoalex.elements.kotlin

import com.github.tnoalex.elements.AbstractElement

data class KotlinFunctionElement(
    override val elementName: String,
    override val elementStartLine: Int,
    override val elementStopLine: Int,
    override val parent: AbstractElement?,
    val parameterNumber: Int,
    val visibility: String?,
    val functionModifier: String?,
    val inheritanceModifier: String?
) : AbstractElement() {
    val functionId: String
        get() = "$elementName@$parameterNumber"

    val isTopLevel: Boolean
        get() = parent is KotlinFunctionElement
}