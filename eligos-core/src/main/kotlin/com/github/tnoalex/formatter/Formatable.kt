package com.github.tnoalex.formatter

import com.github.tnoalex.specs.FormatterSpec

interface Formatable {
    fun unwrap(spec: FormatterSpec): LinkedHashMap<String, Any>
}