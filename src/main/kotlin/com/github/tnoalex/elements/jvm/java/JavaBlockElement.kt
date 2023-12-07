package com.github.tnoalex.elements.jvm.java

import com.github.tnoalex.elements.AbstractElement
import java.util.*

class JavaBlockElement(
    blockStartLine: Int,
    blockStopLine: Int,
    parent: AbstractElement,
    modifier: String
) : JavaElement(null, blockStartLine, blockStopLine, parent, LinkedList(), LinkedList(listOf(modifier)))