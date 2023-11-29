package com.github.tnoalex.elements

import java.util.*

object ElementContainer {
    private val fileElements = LinkedList<FileElement>()

    fun getFileElement(fileName: String): List<FileElement> {
        return fileElements.filter { it.elementName == fileName }
    }

    fun getLastElement(): FileElement {
        return fileElements.last
    }

    fun addFileElement(element: FileElement) {
        fileElements.add(element)
    }
}