package com.github.tnoalex.elements

class BinaryArithmeticExpressionElement(
    expressionStartLine: Int,
    expressionStopLine: Int,
    parent: AbstractElement,
    operator: String
) : ExpressionElement(expressionStartLine, expressionStopLine, operator, parent) {
    init {
        innerElement.add(leftExpression)
        innerElement.add(rightExpression)
    }

    val leftExpression: ExpressionElement
        get() = innerElement.first as ExpressionElement

    val rightExpression: ExpressionElement
        get() = innerElement.last as ExpressionElement


    fun setRightExpression(expressionElement: ExpressionElement) {
        innerElement.removeAt(1)
        innerElement.add(expressionElement)
    }

    fun setLeftExpression(expressionElement: ExpressionElement) {
        innerElement.removeAt(0)
        innerElement.addFirst(expressionElement)
    }

    override fun toString(): String {
        return leftExpression.toString() + operator + rightExpression.toString()
    }
}