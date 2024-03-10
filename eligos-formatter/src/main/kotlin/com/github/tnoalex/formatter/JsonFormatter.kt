package com.github.tnoalex.formatter

import com.google.gson.Gson

class JsonFormatter : IFormatter {
    override val fileExtension: String
        get() = "json"

    override fun format(obj: Any): String {
        return Gson().toJson(obj)
    }
}