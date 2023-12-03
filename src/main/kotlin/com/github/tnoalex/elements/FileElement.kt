package com.github.tnoalex.elements

data class FileElement(
    val fileName: String,
    val fileStartLine: Int,
    val fileStopLine: Int
) : AbstractElement(fileName, fileStartLine, fileStopLine) {

    override val parent: AbstractElement?
        get() = null
}
