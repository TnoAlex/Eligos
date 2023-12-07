package com.github.tnoalex.elements.jvm.java

import com.github.tnoalex.elements.AbstractElement
import java.util.*

class JavaFunctionElement(
    functionName: String?,
    functionStartLine: Int,
    functionStopLine: Int,
    parent: AbstractElement,
    annotations: LinkedList<String>,
    modifiers: LinkedList<String>
) : JavaElement(functionName, functionStartLine, functionStopLine, parent, annotations, modifiers)