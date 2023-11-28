package com.github.tnoalex.elements

data class FileElement(
    override val elementName: String,
    override val elementStartLine: Int,
    override val elementStopLine: Int
) : AbstractElement() {
    override val parent: AbstractElement?
        get() = null
}
