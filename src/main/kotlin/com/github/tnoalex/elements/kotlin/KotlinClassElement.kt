package com.github.tnoalex.elements.kotlin

import com.github.tnoalex.elements.AbstractElement

data class KotlinClassElement(
    override val elementName: String?,
    override val elementStartLine: Int,
    override val elementStopLine: Int,
    override val parent: AbstractElement?,
    val packageName: String?,
    val type: String,
    val modifiers: String?
) : AbstractElement() {
    val qualifiedName: String
        get() = "$packageName.$elementName"
}