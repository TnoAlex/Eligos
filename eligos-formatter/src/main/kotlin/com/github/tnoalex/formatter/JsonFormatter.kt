package com.github.tnoalex.formatter

import com.google.gson.GsonBuilder

class JsonFormatter : IFormatter {
    override val fileExtension: String
        get() = "json"

    override fun format(obj: Any): String {
        return GsonBuilder().disableHtmlEscaping().create().toJson(obj)
    }
}