package com.github.tnoalex.formatter

import com.github.tnoalex.specs.FormatterSpec

class XmlFormatter : IFormatter {
    override val fileExtension: String
        get() = "xml"

    override fun format(obj: Any): String {
        TODO("Not yet implemented")
    }

    override fun write(formatted: String, spec: FormatterSpec) {
        TODO("Not yet implemented")
    }
}