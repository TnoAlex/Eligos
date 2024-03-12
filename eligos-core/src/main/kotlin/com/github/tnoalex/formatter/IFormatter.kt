package com.github.tnoalex.formatter

interface IFormatter {
    val fileExtension: String
    fun format(obj: Any): String
}