package com.github.tnoalex.formatter

import com.github.tnoalex.specs.FormatterSpec


class TextFormatter : IFormatter {
    override val fileExtension: String
        get() = "txt"

    override fun format(obj: Any): String {
        TODO("Not yet implemented")
    }

    override fun write(formatted: String, spec: FormatterSpec) {
        TODO("Not yet implemented")
    }
}