package com.github.tnoalex.elements.jvm.kotlin

import com.github.tnoalex.elements.AbstractElement
import com.github.tnoalex.elements.ExpressionElement

class DeclarationExpressionElement(
    expressionStartLine: Int,
    expressionStopLine: Int,
    parent: AbstractElement,
    identifier: String,
    var type: String?
) : ExpressionElement(expressionStartLine, expressionStopLine, identifier, parent) {

    fun isMutable(): Boolean {
        return operator == "var"
    }

    fun isImmutable(): Boolean {
        return operator == "val"
    }

    fun isNullable(): Boolean {
        return type != null && !type!!.endsWith("?")
    }
}