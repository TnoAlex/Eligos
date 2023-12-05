package com.github.tnoalex.elements

class FileElement(
    fileName: String,
    fileStartLine: Int,
    fileStopLine: Int
) : AbstractElement(fileName, fileStartLine, fileStopLine) {

    override var parent: AbstractElement? = null
    override fun toString(): String {
        return "FileElement(fileName:$elementName)"
    }
}
