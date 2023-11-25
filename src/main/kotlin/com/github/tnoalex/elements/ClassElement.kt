package com.github.tnoalex.elements

data class ClassElement(
    val packageName: String,
    val clazzName: String,
    val startLine: Int,
    val stopLine: Int,
    val modifiers: String,
    val outerClass: String
) {
    val annotations = ArrayList<String>()
    val lineNumber: Int
        get() = stopLine - startLine
    val qualifiedName: String
        get() = "$packageName.$clazzName"
}