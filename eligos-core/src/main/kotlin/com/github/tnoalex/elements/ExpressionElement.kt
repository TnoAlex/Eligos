package com.github.tnoalex.elements

open class ExpressionElement(
    expressionStartLine: Int,
    expressionStopLine: Int,
    val operator: String?,
    override var parent: AbstractElement?
) : AbstractElement(null, expressionStartLine, expressionStopLine) {
    var literally: Any? = null
    fun isLiterally(): Boolean {
        return operator == null
    }
}