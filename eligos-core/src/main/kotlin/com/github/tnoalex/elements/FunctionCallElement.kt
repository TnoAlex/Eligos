package com.github.tnoalex.elements

import java.util.*

class FunctionCallElement(
    line: Int,
    parent: AbstractElement,
    val callTargetName: String,
    val actualParameters: LinkedList<String>
) : PostfixExpressionElement(line, line, parent, "()") {
    override fun toString(): String {
        return "$callTargetName($actualParameters)"
    }
}