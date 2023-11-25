package com.github.tnoalex.elements

data class FunctionElement(
    val functionName: String,
    val parameterNumber: Int,
    val visibility: String,
    val modifiers: String,
    val startLine: Int,
    val stopLine: Int,
    val topLevel: Boolean
) {
    var circleComplexity = 0
    val lineNUmber: Int
        get() = stopLine - startLine
}