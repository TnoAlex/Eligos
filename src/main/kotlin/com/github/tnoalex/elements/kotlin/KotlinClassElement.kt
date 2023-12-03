package com.github.tnoalex.elements.kotlin

import com.github.tnoalex.elements.AbstractElement

data class KotlinClassElement(
    val className: String?,
    val classStartLine: Int,
    val classStopLine: Int,
    override val parent: AbstractElement,
    private val packageName: String?,
    private val type: String,
    private val modifiers: String?
) : AbstractElement(className, classStartLine, classStopLine) {
    val qualifiedName: String
        get() = "$packageName.$className"
}