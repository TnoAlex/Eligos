package com.github.tnoalex.formatter


class TextFormatter : IFormatter {
    override val fileExtension: String
        get() = "txt"

    override fun format(obj: Any): String {
        TODO("Not yet implemented")
    }
}