package com.github.tnoalex.elements

data class FileElement(
    val fileName: String,
    val fileLineNumber: Int
) {
    val classes = ArrayList<ClassElement>()
    val functions = ArrayList<FunctionElement>()
}
