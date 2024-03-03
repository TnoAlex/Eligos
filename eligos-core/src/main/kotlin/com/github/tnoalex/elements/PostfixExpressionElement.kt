package com.github.tnoalex.elements

open class PostfixExpressionElement(
    expressionStartLine: Int,
    expressionStopLine: Int,
    parent: AbstractElement,
    operator: String
) : ExpressionElement(expressionStartLine, expressionStopLine, operator, parent) {
    override fun toString(): String {
        return innerElement.first.toString() + operator
    }
}